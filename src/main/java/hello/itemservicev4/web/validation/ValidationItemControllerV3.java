package hello.itemservicev4.web.validation;

import hello.itemservicev4.domain.item.Item;
import hello.itemservicev4.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/validation/v3/items")
@RequiredArgsConstructor
public class ValidationItemControllerV3 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v3/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v3/addForm";
    }

    @PostMapping("/add")
    public String addItem(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        // BindingResult : 스프링이 제공하는 검증 오류를 보관하는 객체
        // @ModelAttribute 는 자동으로 model 에 등록함.
        // @Validated 는 Item 에 대해서 검증을 수행한다.
        // 검증을 하고 bindResult 에 결과 값이 담긴다.

        // 특정 필드가 아닌 복합 룰 검증
        // 가격이 있으면서 수량도 있으면
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            // 가격 * 수량이 10,000 보다 작으면
            if (resultPrice < 10000) {
                // 특정 필드랑 비교하기 어려움 -> global 오류
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        // 검증에 실패하면 다시 입력 폼으로
        // errors 가 있으면
        if (bindingResult.hasErrors()) {
            log.info("errors = {} ", bindingResult);
            // bindingResult 는 자동으로 view 에 넘어감.
//            model.addAttribute("errors", errors);
            return "validation/v3/addForm";
        }

        // 성공 로직

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v3/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @Validated @ModelAttribute Item item, BindingResult bindingResult) {

        // 특정 필드가 아닌 복합 룰 검증
        // 가격이 있으면서 수량도 있으면
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            // 가격 * 수량이 10,000 보다 작으면
            if (resultPrice < 10000) {
                // 특정 필드랑 비교하기 어려움 -> global 오류
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        // 검증에 실패하면 다시 입력 폼으로
        // errors 가 있으면
        if (bindingResult.hasErrors()) {
            log.info("errors = {} ", bindingResult);
            // bindingResult 는 자동으로 view 에 넘어감.
//            model.addAttribute("errors", errors);
            return "validation/v3/editForm";
        }

        itemRepository.update(itemId, item);
        return "redirect:/validation/v3/items/{itemId}";
    }

}

