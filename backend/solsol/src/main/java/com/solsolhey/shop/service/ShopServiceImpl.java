package com.solsolhey.shop.service;

import com.solsolhey.shop.domain.Item;
import com.solsolhey.shop.domain.Order;
import com.solsolhey.shop.domain.UserItem;
import com.solsolhey.shop.dto.GifticonResponse;
import com.solsolhey.shop.dto.ItemResponse;
import com.solsolhey.shop.dto.OrderRequest;
import com.solsolhey.shop.dto.OrderResponse;
import com.solsolhey.shop.repository.ItemRepository;
import com.solsolhey.shop.repository.OrderRepository;
import com.solsolhey.shop.repository.UserItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ShopServiceImpl implements ShopService {
    
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final UserItemRepository userItemRepository;
    
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
        
        // 수량 검증
        int quantity = request.getQuantity() != null ? request.getQuantity() : 1;
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        
        // 총 가격 계산
        int totalPrice = item.getPrice() * quantity;
        
        // TODO: 사용자 포인트 차감 로직 추가 (현재는 생략)
        
        // 사용자 아이템에 추가 또는 수량 증가
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
            log.info("새로운 사용자 아이템 추가: 사용자 {}, 아이템 {}, 수량: {}", userId, item.getId(), quantity);
        }
        
        // 주문 생성
        return Order.builder()
                .userId(userId)
                .itemId(item.getId())
                .quantity(quantity)
                .totalPrice(totalPrice)
                .orderType(Order.OrderType.ITEM)
                .status(Order.OrderStatus.COMPLETED)
                .build();
    }
    
    private Order processGifticonOrder(Long userId, OrderRequest request) {
        // SKU 검증
        if (request.getSku() == null || request.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("기프티콘 구매 시 SKU는 필수입니다.");
        }
        
        // Mock 기프티콘 가격 (실제로는 외부 API에서 가격 조회)
        int gifticonPrice = getGifticonPrice(request.getSku());
        
        // TODO: 사용자 포인트 차감 로직 추가 (현재는 생략)
        
        // 주문 생성
        return Order.builder()
                .userId(userId)
                .quantity(1)
                .totalPrice(gifticonPrice)
                .orderType(Order.OrderType.GIFTICON)
                .status(Order.OrderStatus.COMPLETED)
                .gifticonSku(request.getSku())
                .build();
    }
    
    @Override
    public List<GifticonResponse> getGifticons() {
        log.info("기프티콘 목록 조회 (Mock)");
        
        // Mock 데이터 반환
        return List.of(
                GifticonResponse.builder()
                        .sku("STARBUCKS_AMERICANO")
                        .name("스타벅스 아메리카노")
                        .description("진한 에스프레소에 물을 더한 깔끔한 맛의 아메리카노")
                        .price(4500)
                        .imageUrl("https://image.istarbucks.co.kr/upload/store/skuimg/2021/04/[9200000000038]_20210415154152024.jpg")
                        .build(),
                GifticonResponse.builder()
                        .sku("STARBUCKS_LATTE")
                        .name("스타벅스 카페라떼")
                        .description("진한 에스프레소와 스팀밀크가 조화로운 부드러운 맛")
                        .price(5000)
                        .imageUrl("https://image.istarbucks.co.kr/upload/store/skuimg/2021/04/[9200000000077]_20210415154521370.jpg")
                        .build(),
                GifticonResponse.builder()
                        .sku("GONGCHA_BROWN_SUGAR")
                        .name("공차 흑당 밀크티")
                        .description("진한 흑당 시럽과 부드러운 밀크티의 조화")
                        .price(5500)
                        .imageUrl("https://www.gong-cha.co.kr/upload/menu/2019090614265920190906142659.png")
                        .build(),
                GifticonResponse.builder()
                        .sku("BASKIN_ICE_CREAM")
                        .name("배스킨라빈스 아이스크림")
                        .description("다양한 맛의 프리미엄 아이스크림")
                        .price(3000)
                        .imageUrl("https://www.baskinrobbins.co.kr/upload/product/1635216951063_top.png")
                        .build()
        );
    }
    
    @Override
    @Transactional
    public String redeemGifticon(Long userId, Long gifticonId) {
        log.info("기프티콘 사용 요청 - 사용자: {}, 기프티콘 ID: {}", userId, gifticonId);
        
        // Mock 처리 (실제로는 주문 내역에서 기프티콘 조회 및 사용 처리)
        if (gifticonId == null || gifticonId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 기프티콘 ID입니다.");
        }
        
        // TODO: 실제 기프티콘 사용 로직 구현
        // 1. 사용자가 해당 기프티콘을 구매했는지 확인
        // 2. 이미 사용된 기프티콘인지 확인
        // 3. 기프티콘 사용 처리
        // 4. 외부 API 호출 (실제 기프티콘 발급)
        
        log.info("기프티콘 사용 완료 - 사용자: {}, 기프티콘 ID: {}", userId, gifticonId);
        return "기프티콘이 성공적으로 사용되었습니다. 사용 내역은 마이페이지에서 확인하실 수 있습니다.";
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
}
