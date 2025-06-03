package tqs.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tqs.backend.model.Discount;
import tqs.backend.model.Station;
import tqs.backend.model.enums.ChargerType;
import tqs.backend.repository.DiscountRepository;
import tqs.backend.repository.StationRepository;
import tqs.backend.service.DiscountService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DiscountController.class)
@Import(DiscountControllerTest.MockConfig.class)
@ActiveProfiles("test")
class DiscountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DiscountService discountService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public DiscountService discountService() {
            return mock(DiscountService.class);
        }

        @Bean
        public StationRepository stationRepository() {
            return mock(StationRepository.class);
        }

        @Bean
        public DiscountRepository discountRepository() {
            return mock(DiscountRepository.class);
        }



        @Bean
        public Validator validator() {
            return new LocalValidatorFactoryBean();
        }
    }

    @TestConfiguration
    @EnableWebSecurity
    static class SecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/discounts/**").permitAll()
                    .anyRequest().authenticated());
            return http.build();
        }
    }


    @Test
    void createDiscount_ValidRequest_ReturnsOk() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "stationId", 1L,
                "chargerType", "AC_STANDARD",
                "dayOfWeek", 2,
                "startHour", 9,
                "endHour", 18,
                "discountPercent", 20.0,
                "active", true
        );

        Station station = Station.builder().id(1L).build();

        Discount savedDiscount = Discount.builder()
                .id(10L)
                .station(station)
                .chargerType(ChargerType.AC_STANDARD)
                .dayOfWeek(2)
                .startHour(9)
                .endHour(18)
                .discountPercent(20.0)
                .active(true)
                .build();

        when(discountService.createDiscount(
                eq(1L), eq(ChargerType.AC_STANDARD), eq(2), eq(9), eq(18), eq(20.0), eq(true)))
                .thenReturn(savedDiscount);

        mockMvc.perform(post("/api/discounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedDiscount.getId()))
                .andExpect(jsonPath("$.chargerType").value("AC_STANDARD"))
                .andExpect(jsonPath("$.dayOfWeek").value(2))
                .andExpect(jsonPath("$.discountPercent").value(20.0))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createDiscount_StationNotFound_ReturnsNotFound() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "stationId", 999L,
                "chargerType", "AC_STANDARD",
                "dayOfWeek", 2,
                "startHour", 9,
                "endHour", 18,
                "discountPercent", 20.0,
                "active", true
        );

        when(discountService.createDiscount(anyLong(), any(), anyInt(), anyInt(), anyInt(), anyDouble(), anyBoolean()))
                .thenThrow(new IllegalArgumentException("Station not found"));

        mockMvc.perform(post("/api/discounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Station not found"));    }

    @Test
    void getAllDiscounts_ReturnsList() throws Exception {
        List<Discount> discounts = List.of(
                Discount.builder()
                        .id(1L)
                        .chargerType(ChargerType.AC_STANDARD)
                        .dayOfWeek(1)
                        .discountPercent(10.0)
                        .active(true)
                        .build(),
                Discount.builder()
                        .id(2L)
                        .chargerType(ChargerType.DC_FAST)
                        .dayOfWeek(5)
                        .discountPercent(25.0)
                        .active(false)
                        .build()
        );

        when(discountService.getAllDiscounts()).thenReturn(discounts);

        mockMvc.perform(get("/api/discounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(discounts.size()))
                .andExpect(jsonPath("$[0].chargerType").value("AC_STANDARD"))
                .andExpect(jsonPath("$[1].chargerType").value("DC_FAST"));
    }

    @Test
    void getDiscountById_ExistingId_ReturnsDiscount() throws Exception {
        Long discountId = 1L;

        Discount discount = Discount.builder()
                .id(discountId)
                .chargerType(ChargerType.AC_STANDARD)
                .dayOfWeek(3)
                .discountPercent(15.0)
                .active(true)
                .build();

        when(discountService.getDiscount(discountId)).thenReturn(Optional.of(discount));

        mockMvc.perform(get("/api/discounts/{id}", discountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(discountId))
                .andExpect(jsonPath("$.chargerType").value("AC_STANDARD"));
    }

    @Test
    void getDiscountById_NotFound_ReturnsNotFound() throws Exception {
        Long discountId = 999L;

        when(discountService.getDiscount(discountId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/discounts/{id}", discountId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Discount not found"));    }

    @Test
    void updateDiscount_ValidRequest_ReturnsUpdated() throws Exception {
        reset(discountService);
        Long discountId = 1L;
        Map<String, Object> requestBody = Map.of(
                "stationId", 1L,
                "chargerType", "DC_FAST",
                "dayOfWeek", 4,
                "startHour", 10,
                "endHour", 19,
                "discountPercent", 30.0,
                "active", false
        );

        Station station = Station.builder().id(1L).build();

        Discount updatedDiscount = Discount.builder()
                .id(discountId)
                .station(station)
                .chargerType(ChargerType.DC_FAST)
                .dayOfWeek(4)
                .startHour(10)
                .endHour(19)
                .discountPercent(30.0)
                .active(false)
                .build();

        when(discountService.updateDiscount(
                eq(discountId), eq(1L), eq(ChargerType.DC_FAST), eq(4), eq(10), eq(19), eq(30.0), eq(false)))
                .thenReturn(updatedDiscount);

        mockMvc.perform(put("/api/discounts/{id}", discountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(discountId))
                .andExpect(jsonPath("$.chargerType").value("DC_FAST"))
                .andExpect(jsonPath("$.discountPercent").value(30.0))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateDiscount_NotFound_ReturnsNotFound() throws Exception {
        Long discountId = 999L;

        Map<String, Object> requestBody = Map.of(
                "stationId", 1L,
                "chargerType", "DC_FAST",
                "dayOfWeek", 4,
                "startHour", 10,
                "endHour", 19,
                "discountPercent", 30.0,
                "active", false
        );

        when(discountService.updateDiscount(anyLong(), anyLong(), any(), anyInt(), anyInt(), anyInt(), anyDouble(), anyBoolean()))
                .thenThrow(new IllegalArgumentException("Discount not found"));

        mockMvc.perform(put("/api/discounts/{id}", discountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Discount not found"));    
    }

    @Test
    void deleteDiscount_ExistingId_ReturnsNoContent() throws Exception {
        Long discountId = 5L;

        doNothing().when(discountService).deleteDiscount(discountId);

        mockMvc.perform(delete("/api/discounts/{id}", discountId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteDiscount_NotFound_ReturnsNotFound() throws Exception {
        Long discountId = 999L;

        doThrow(new IllegalArgumentException("Discount not found")).when(discountService).deleteDiscount(discountId);

        mockMvc.perform(delete("/api/discounts/{id}", discountId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Discount not found"));    }
    }
