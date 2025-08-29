package com.solsolhey.shop.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solsolhey.point.dto.request.PointSpendRequest;
import com.solsolhey.point.entity.PointTransaction.ReferenceType;
import com.solsolhey.point.service.PointService;
import com.solsolhey.shop.domain.Item;
import com.solsolhey.shop.domain.Order;
import com.solsolhey.shop.domain.UserGifticon;
import com.solsolhey.shop.domain.UserItem;
import com.solsolhey.mascot.repository.MascotRepository;
import com.solsolhey.shop.dto.GifticonResponse;
import com.solsolhey.shop.dto.ItemResponse;
import com.solsolhey.shop.dto.OrderRequest;
import com.solsolhey.shop.dto.OrderResponse;
import com.solsolhey.shop.repository.ItemRepository;
import com.solsolhey.shop.repository.OrderRepository;
import com.solsolhey.shop.repository.UserGifticonRepository;
import com.solsolhey.shop.repository.UserItemRepository;
import com.solsolhey.user.entity.User;
import com.solsolhey.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ShopServiceImpl implements ShopService {
    
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final UserItemRepository userItemRepository;
    private final UserGifticonRepository userGifticonRepository;
    private final PointService pointService;
    private final UserRepository userRepository;
    private final MascotRepository mascotRepository;
    
    @Override
    public List<ItemResponse> getItems(String type) {
        List<Item> items;
        
        if (type == null || type.trim().isEmpty()) {
            items = itemRepository.findByIsActiveTrue();
            log.info("전체 활성 상품 조회: {} 개", items.size());
        } else {
            try {
                Item.ItemType itemType = Item.ItemType.valueOf(type.toUpperCase());
                items = itemRepository.findByTypeAndIsActiveTrue(itemType);
                log.info("타입 {} 활성 상품 조회: {} 개", type, items.size());
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 상품 타입: {}", type);
                throw new IllegalArgumentException("유효하지 않은 상품 타입입니다: " + type);
            }
        }
        
        return items.stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ItemResponse> getItemsWithOwnership(Long userId, String type) {
        List<Item> items;
        
        if (type == null || type.trim().isEmpty()) {
            items = itemRepository.findByIsActiveTrue();
            log.info("사용자 {} 전체 활성 상품 조회 (보유 여부 포함): {} 개", userId, items.size());
        } else {
            try {
                Item.ItemType itemType = Item.ItemType.valueOf(type.toUpperCase());
                items = itemRepository.findByTypeAndIsActiveTrue(itemType);
                log.info("사용자 {} 타입 {} 활성 상품 조회 (보유 여부 포함): {} 개", userId, type, items.size());
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 상품 타입: {}", type);
                throw new IllegalArgumentException("유효하지 않은 상품 타입입니다: " + type);
            }
        }
        
        return items.stream()
                .map(item -> {
                    Boolean owned = userItemRepository.existsByUserIdAndItemId(userId, item.getId());
                    log.debug("아이템 {} 사용자 {} 보유 여부: {}", item.getId(), userId, owned);
                    return ItemResponse.fromWithOwnership(item, owned);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ItemResponse> getPublicItems(String type) {
        List<Item> items;
        
        if (type == null || type.trim().isEmpty()) {
            items = itemRepository.findByIsActiveTrue();
            log.info("공개 전체 활성 상품 조회: {} 개", items.size());
        } else {
            try {
                Item.ItemType itemType = Item.ItemType.valueOf(type.toUpperCase());
                items = itemRepository.findByTypeAndIsActiveTrue(itemType);
                log.info("공개 타입 {} 활성 상품 조회: {} 개", type, items.size());
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 상품 타입: {}", type);
                throw new IllegalArgumentException("유효하지 않은 상품 타입입니다: " + type);
            }
        }
        
        // 공개용으로는 기본 상품 정보만 반환 (소유권 정보 제외)
        return items.stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, OrderRequest request) {
        log.info("주문 처리 시작 - 사용자: {}, 타입: {}", userId, request.getType());
        
        // 주문 타입 검증
        Order.OrderType orderType;
        try {
            orderType = Order.OrderType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 주문 타입입니다: " + request.getType());
        }
        
        Order order;
        String message;
        
        if (orderType == Order.OrderType.ITEM) {
            order = processItemOrder(userId, request);
            message = "상품 구매가 완료되었습니다!";
        } else {
            order = processGifticonOrder(userId, request);
            message = "기프티콘 구매가 완료되었습니다!";
        }
        
        Order savedOrder = orderRepository.save(order);
        log.info("주문 완료 - 주문 ID: {}, 총 가격: {}", savedOrder.getId(), savedOrder.getTotalPrice());
        
        return OrderResponse.from(savedOrder, message);
    }
    
    private Order processItemOrder(Long userId, OrderRequest request) {
        // 상품 ID 검증
        if (request.getItemId() == null) {
            throw new IllegalArgumentException("상품 구매 시 상품 ID는 필수입니다.");
        }
        
        // 상품 존재 여부 및 활성 상태 확인
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + request.getItemId()));
        
        if (!item.getIsActive()) {
            throw new IllegalStateException("비활성 상품입니다: " + item.getName());
        }

        // 레벨 검증: 사용자 마스코트 레벨이 아이템 필요 레벨 이상인지 확인
        int requiredLevel = Optional.ofNullable(item.getRequiredLevel()).orElse(1);
        int userLevel = mascotRepository.findByUserId(userId)
                .map(m -> Optional.ofNullable(m.getLevel()).orElse(1))
                .orElse(1);
        if (userLevel < requiredLevel) {
            throw new IllegalArgumentException("Lv." + requiredLevel + " 이상부터 구매할 수 있습니다.");
        }
        
        // 수량 검증
        int quantity = request.getQuantity() != null ? request.getQuantity() : 1;
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        
        // 총 가격 계산
        int totalPrice = item.getPrice() * quantity;
        
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        
        // 1단계: 주문을 PENDING 상태로 생성
        Order order = Order.builder()
                .userId(userId)
                .itemId(item.getId())
                .quantity(quantity)
                .totalPrice(totalPrice)
                .orderType(Order.OrderType.ITEM)
                .status(Order.OrderStatus.PENDING) // 초기 상태
                .build();
        
        Order savedOrder = orderRepository.save(order);
        log.info("주문 생성 완료 - 주문 ID: {}, 상태: PENDING", savedOrder.getId());
        
        try {
            // 2단계: 생성된 주문 ID를 사용하여 포인트 차감
            PointSpendRequest spendRequest = new PointSpendRequest(
                totalPrice, 
                "상품 구매: " + item.getName(), 
                savedOrder.getId(), // 생성된 주문 ID 사용
                ReferenceType.PURCHASE
            );
            pointService.spendPoints(user, spendRequest);
            log.info("포인트 차감 완료: userId={}, amount={}, item={}, orderId={}", userId, totalPrice, item.getName(), savedOrder.getId());
            
            // 3단계: 사용자 아이템에 추가 또는 수량 증가
            Optional<UserItem> existingUserItem = userItemRepository.findByUserIdAndItemId(userId, item.getId());
            if (existingUserItem.isPresent()) {
                UserItem userItem = existingUserItem.get();
                userItem.addQuantity(quantity);
                userItemRepository.save(userItem);
                log.info("기존 사용자 아이템 수량 증가: 사용자 {}, 아이템 {}, 추가 수량: {}", userId, item.getId(), quantity);
            } else {
                UserItem userItem = UserItem.builder()
                        .userId(userId)
                        .itemId(item.getId())
                        .quantity(quantity)
                        .build();
                userItemRepository.save(userItem);
                log.info("새로운 사용자 아이템 추가: 사용자 {}, 아이템 {}, 추가 수량: {}", userId, item.getId(), quantity);
            }
            
            // 4단계: 포인트 차감 성공 시 주문 상태를 COMPLETED로 변경
            savedOrder.setStatus(Order.OrderStatus.COMPLETED);
            orderRepository.save(savedOrder);
            log.info("주문 상태 업데이트 완료 - 주문 ID: {}, 상태: COMPLETED", savedOrder.getId());
            
        } catch (RuntimeException e) {
            // 5단계: 포인트 차감 실패 시 주문 상태를 CANCELLED로 변경 후 원래 예외를 그대로 전파
            savedOrder.setStatus(Order.OrderStatus.CANCELLED);
            orderRepository.save(savedOrder);
            log.error("포인트 차감 실패로 주문 취소: userId={}, amount={}, item={}, orderId={}", userId, totalPrice, item.getName(), savedOrder.getId(), e);
            throw e;
        } catch (Exception e) {
            // 체크 예외는 불가피하게 래핑
            savedOrder.setStatus(Order.OrderStatus.CANCELLED);
            orderRepository.save(savedOrder);
            log.error("포인트 차감 실패로 주문 취소(체크 예외): userId={}, amount={}, item={}, orderId={}", userId, totalPrice, item.getName(), savedOrder.getId(), e);
            throw new IllegalStateException("포인트 차감 처리 중 알 수 없는 오류가 발생했습니다.");
        }
        
        return savedOrder;
    }
    
    private Order processGifticonOrder(Long userId, OrderRequest request) {
        // SKU 검증
        if (request.getSku() == null || request.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("기프티콘 구매 시 SKU는 필수입니다.");
        }
        
        // Mock 기프티콘 가격 (실제로는 외부 API에서 가격 조회)
        int gifticonPrice = getGifticonPrice(request.getSku());
        
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        
        // 1단계: 주문을 PENDING 상태로 생성
        Order order = Order.builder()
                .userId(userId)
                .quantity(1)
                .totalPrice(gifticonPrice)
                .orderType(Order.OrderType.GIFTICON)
                .status(Order.OrderStatus.PENDING) // 초기 상태
                .gifticonSku(request.getSku())
                .build();
        
        Order savedOrder = orderRepository.save(order);
        log.info("기프티콘 주문 생성 완료 - 주문 ID: {}, 상태: PENDING", savedOrder.getId());
        
        try {
            // 2단계: 생성된 주문 ID를 사용하여 포인트 차감
            PointSpendRequest spendRequest = new PointSpendRequest(
                gifticonPrice, 
                "기프티콘 구매: " + request.getSku(), 
                savedOrder.getId(), // 생성된 주문 ID 사용
                ReferenceType.PURCHASE
            );
            pointService.spendPoints(user, spendRequest);
            log.info("포인트 차감 완료: userId={}, amount={}, sku={}, orderId={}", userId, gifticonPrice, request.getSku(), savedOrder.getId());
            
            // 3단계: 포인트 차감 성공 시 주문 상태를 COMPLETED로 변경
            savedOrder.setStatus(Order.OrderStatus.COMPLETED);
            orderRepository.save(savedOrder);
            log.info("기프티콘 주문 상태 업데이트 완료 - 주문 ID: {}, 상태: COMPLETED", savedOrder.getId());

            // 4단계: 사용자 보관함에 기프티콘 저장 (바코드/만료 포함)
            GifticonMeta meta = getGifticonMeta(request.getSku());
            LocalDateTime purchasedAt = savedOrder.getCreatedAt() != null ? savedOrder.getCreatedAt() : LocalDateTime.now();
            UserGifticon gifticon = UserGifticon.builder()
                    .userId(userId)
                    .sku(request.getSku())
                    .name(meta.name)
                    .imageUrl(meta.imageUrl)
                    .barcode(generateBarcode())
                    .status(UserGifticon.Status.ACTIVE)
                    .expiresAt(purchasedAt.plusYears(1))
                    .build();
            userGifticonRepository.save(gifticon);
            log.info("보관함 기프티콘 생성 - userId={}, sku={}, gifticonId={}", userId, request.getSku(), gifticon.getId());
            
        } catch (RuntimeException e) {
            // 4단계: 포인트 차감 실패 시 주문 상태를 CANCELLED로 변경 후 원래 예외를 그대로 전파
            savedOrder.setStatus(Order.OrderStatus.CANCELLED);
            orderRepository.save(savedOrder);
            log.error("포인트 차감 실패로 기프티콘 주문 취소: userId={}, amount={}, sku={}, orderId={}", userId, gifticonPrice, request.getSku(), savedOrder.getId(), e);
            throw e;
        } catch (Exception e) {
            savedOrder.setStatus(Order.OrderStatus.CANCELLED);
            orderRepository.save(savedOrder);
            log.error("포인트 차감 실패로 기프티콘 주문 취소(체크 예외): userId={}, amount={}, sku={}, orderId={}", userId, gifticonPrice, request.getSku(), savedOrder.getId(), e);
            throw new IllegalStateException("포인트 차감 처리 중 알 수 없는 오류가 발생했습니다.");
        }
        
        return savedOrder;
    }
    
    @Override
    public List<GifticonResponse> getGifticons() {
        log.info("기프티콘 목록 조회 (Mock)");
        return getGifticonCatalog();
    }
    
    @Override
    @Transactional
    public String redeemGifticon(Long userId, Long gifticonId) {
        log.info("기프티콘 사용 요청 - 사용자: {}, 기프티콘 ID: {}", userId, gifticonId);
        var gifticon = userGifticonRepository.findByIdAndUserId(gifticonId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 기프티콘을 찾을 수 없습니다."));

        // 만료 체크
        if (gifticon.getExpiresAt() != null && gifticon.getExpiresAt().isBefore(LocalDateTime.now())) {
            gifticon.setStatus(UserGifticon.Status.EXPIRED);
            userGifticonRepository.save(gifticon);
            throw new IllegalArgumentException("만료된 기프티콘입니다.");
        }
        if (gifticon.getStatus() == UserGifticon.Status.REDEEMED) {
            throw new IllegalArgumentException("이미 사용된 기프티콘입니다.");
        }
        gifticon.setStatus(UserGifticon.Status.REDEEMED);
        userGifticonRepository.save(gifticon);
        log.info("기프티콘 사용 완료 - 사용자: {}, 보관함ID: {}", userId, gifticonId);
        return "기프티콘 사용이 완료되었습니다.";
    }
    
    /**
     * Mock 기프티콘 가격 조회
     */
    private int getGifticonPrice(String sku) {
        return switch (sku.toUpperCase()) {
            case "STARBUCKS_AMERICANO" -> 4500;
            case "STARBUCKS_LATTE" -> 5000;
            case "GONGCHA_BROWN_SUGAR" -> 5500;
            case "BASKIN_ICE_CREAM" -> 3000;
            default -> {
                log.warn("알 수 없는 기프티콘 SKU: {}", sku);
                throw new IllegalArgumentException("지원하지 않는 기프티콘입니다: " + sku);
            }
        };
    }

    @Override
    public List<com.solsolhey.shop.dto.PurchasedGifticonResponse> getPurchasedGifticons(Long userId) {
        var list = userGifticonRepository.findByUserIdOrderByCreatedAtDesc(userId);
        var now = LocalDateTime.now();
        boolean changed = false;
        for (var g : list) {
            changed |= normalizeGifticonExpiry(g, now);
        }
        if (changed) userGifticonRepository.saveAll(list);
        return list.stream().map(com.solsolhey.shop.dto.PurchasedGifticonResponse::from).collect(Collectors.toList());
    }

    @Override
    public com.solsolhey.shop.dto.PurchasedGifticonDetailResponse getPurchasedGifticon(Long userId, Long id) {
        var g = userGifticonRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 기프티콘을 찾을 수 없습니다."));
        if (normalizeGifticonExpiry(g, LocalDateTime.now())) {
            userGifticonRepository.save(g);
        }
        return com.solsolhey.shop.dto.PurchasedGifticonDetailResponse.from(g);
    }

    /**
     * 만료/만료일 보정 공통 처리
     * - ACTIVE 상태에서 만료되었으면 EXPIRED로 변경
     * - 만료일이 없으면 생성일 기준 1년으로 세팅
     * @return 변경 여부
     */
    private boolean normalizeGifticonExpiry(UserGifticon g, LocalDateTime now) {
        boolean changed = false;
        if (g.getStatus() == UserGifticon.Status.ACTIVE && g.getExpiresAt() != null && g.getExpiresAt().isBefore(now)) {
            g.setStatus(UserGifticon.Status.EXPIRED);
            changed = true;
        }
        if (g.getExpiresAt() == null) {
            LocalDateTime base = g.getCreatedAt() != null ? g.getCreatedAt() : now;
            g.setExpiresAt(base.plusYears(1));
            changed = true;
        }
        return changed;
    }

    private String generateBarcode() {
        // 간단한 12자리 코드 생성 (실서비스는 외부 발급/규격 필요)
        String ts = String.valueOf(System.currentTimeMillis());
        return ts.substring(ts.length() - 12);
    }

    private GifticonMeta getGifticonMeta(String sku) {
        return getGifticonCatalog().stream()
                .filter(g -> g.getSku().equalsIgnoreCase(sku))
                .findFirst()
                .map(g -> new GifticonMeta(g.getName(), g.getImageUrl()))
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 기프티콘입니다: " + sku));
    }

    private List<GifticonResponse> getGifticonCatalog() {
        return List.of(
                GifticonResponse.builder()
                        .sku("STARBUCKS_AMERICANO")
                        .name("스타벅스 아메리카노")
                        .description("진한 에스프레소에 물을 더한 깔끔한 맛의 아메리카노")
                        .price(4500)
                        .imageUrl("/gifticons/originals/gifticon_iced_americano.png")
                        .build(),
                GifticonResponse.builder()
                        .sku("STARBUCKS_LATTE")
                        .name("스타벅스 카페라떼")
                        .description("진한 에스프레소와 스팀밀크가 조화로운 부드러운 맛")
                        .price(5000)
                        .imageUrl("/gifticons/originals/gifticon_iced_vanilla_latte.png")
                        .build(),
                GifticonResponse.builder()
                        .sku("GONGCHA_BROWN_SUGAR")
                        .name("공차 흑당 밀크티")
                        .description("진한 흑당 시럽과 부드러운 밀크티의 조화")
                        .price(5500)
                        .imageUrl("/gifticons/originals/gifticon_gongcha_brownsugar_milktea.png")
                        .build(),
                GifticonResponse.builder()
                        .sku("BASKIN_ICE_CREAM")
                        .name("배스킨라빈스 아이스크림")
                        .description("다양한 맛의 프리미엄 아이스크림")
                        .price(3000)
                        .imageUrl("/gifticons/originals/gifticon_baskin_icecream.png")
                        .build()
        );
    }

    private record GifticonMeta(String name, String imageUrl) {}
}
