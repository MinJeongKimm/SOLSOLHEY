package com.solsolhey.shop.service;

import com.solsolhey.shop.dto.GifticonResponse;
import com.solsolhey.shop.dto.ItemResponse;
import com.solsolhey.shop.dto.OrderRequest;
import com.solsolhey.shop.dto.OrderResponse;
import com.solsolhey.shop.dto.PurchasedGifticonDetailResponse;
import com.solsolhey.shop.dto.PurchasedGifticonResponse;

import java.util.List;

public interface ShopService {
    
    /**
     * 상품 목록 조회
     * @param type 상품 타입 (EQUIP, BACKGROUND) - null이면 전체 조회
     * @return 상품 목록
     */
    List<ItemResponse> getItems(String type);
    
    /**
     * 사용자별 보유 여부를 포함한 상품 목록 조회
     * @param userId 사용자 ID
     * @param type 상품 타입 (EQUIP, BACKGROUND) - null이면 전체 조회
     * @return 보유 여부가 포함된 상품 목록
     */
    List<ItemResponse> getItemsWithOwnership(Long userId, String type);
    
    /**
     * 공개 상품 목록 조회 (소유권 정보 제외)
     * @param type 상품 타입 (EQUIP, BACKGROUND) - null이면 전체 조회
     * @return 공개 상품 목록
     */
    List<ItemResponse> getPublicItems(String type);
    
    /**
     * 주문 처리 (상품 구매 또는 기프티콘 구매)
     * @param userId 사용자 ID
     * @param request 주문 요청
     * @return 주문 응답
     */
    OrderResponse createOrder(Long userId, OrderRequest request);
    
    /**
     * 기프티콘 목록 조회 (Mock)
     * @return 기프티콘 목록
     */
    List<GifticonResponse> getGifticons();
    
    /**
     * 기프티콘 사용 (Mock)
     * @param userId 사용자 ID
     * @param gifticonId 기프티콘 ID
     * @return 사용 결과 메시지
     */
    String redeemGifticon(Long userId, Long gifticonId);

    /**
     * 사용자가 구매한 기프티콘 목록 조회 (보관함)
     */
    List<PurchasedGifticonResponse> getPurchasedGifticons(Long userId);

    /**
     * 사용자가 구매한 기프티콘 상세 조회
     */
    PurchasedGifticonDetailResponse getPurchasedGifticon(Long userId, Long id);
}
