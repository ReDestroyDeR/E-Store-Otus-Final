package ru.red.deliveryservice.mapper;

import org.springframework.stereotype.Component;
import ru.red.billing.avro.OrderAcknowledgmentKey;
import ru.red.delivery.avro.Address;
import ru.red.delivery.avro.DeliveryAggregate;
import ru.red.deliveryservice.dto.AddressDTO;
import ru.red.deliveryservice.dto.DeliveryDTO;
import ru.red.deliveryservice.dto.ProductInfoDTO;
import ru.red.order.avro.ProductInfo;

import java.util.List;
import java.util.Map;

@Component
public class DeliveryMapper {
    public DeliveryDTO keyAndAggregateToDTO(CharSequence orderId, Long userId, DeliveryAggregate aggregate) {
        return keyAndAggregateToDTO(new OrderAcknowledgmentKey(orderId, userId), aggregate);
    }

    public DeliveryDTO keyAndAggregateToDTO(OrderAcknowledgmentKey key, DeliveryAggregate aggregate) {
        var dto = new DeliveryDTO();
        dto.setStatus(aggregate.getStatus());
        dto.setAddress(addressToDTO(aggregate.getAddress()));
        dto.setProducts(productInfoMapToDTOList(aggregate.getItems()));
        dto.setUserId(key.getUserId());
        dto.setOrderId(key.getOrderId().toString());
        return dto;
    }

    public DeliveryDTO producerRecordExplodedToDeliveryDTO(OrderAcknowledgmentKey key, Address address,
                                                           Map<CharSequence, ProductInfo> items) {
        var dto = new DeliveryDTO();
        dto.setOrderId(key.getOrderId().toString());
        dto.setUserId(key.getUserId());
        dto.setAddress(addressToDTO(address));
        dto.setProducts(productInfoMapToDTOList(items));
        return dto;
    }

    private AddressDTO addressToDTO(Address address) {
        var addressDTO = new AddressDTO();
        addressDTO.setCity(address.getCity().toString());
        addressDTO.setStreet(address.getStreet().toString());
        addressDTO.setBuilding(address.getBuilding().toString());
        addressDTO.setApartment(address.getApartment());
        return addressDTO;
    }

    private List<ProductInfoDTO> productInfoMapToDTOList(Map<CharSequence, ProductInfo> items) {
        return items.entrySet()
                .stream()
                .map(entry -> {
                    var productInfoDTO = new ProductInfoDTO();
                    productInfoDTO.setName(entry.getKey().toString());
                    productInfoDTO.setUnits(entry.getValue().getUnits());
                    return productInfoDTO;
                }).toList();
    }
}
