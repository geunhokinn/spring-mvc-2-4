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
    private final ItemValidator itemValidator;

    // 컨트롤러가 호출될 때마다 검증기를 넣음.
    @InitBinder
    public void init(WebDataBinder dataBinder) {
        dataBinder.addValidators(itemValidator);
    }

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

//    @PostMapping("/add")
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        // BindingResult : 스프링이 제공하는 검증 오류를 보관하는 객체
        // @ModelAttribute 는 자동으로 model 에 등록함.

        // 검증 로직

        // 필드 오류 검증
        // 글자가 없으면
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));
        }
        // 가격이 없고 1,000 ~ 1,000,000 이 아니면
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price", "가격은 1,000 ~ 1,000,000 까지 허용합니다."));
        }
        // 수량이 없고 9,999 이상이면
        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999 까지 허용합니다."));
        }

        // 특정 필드가 아닌 복합 룰 검증
        // 가격이 있으면서 수량도 있으면
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            // 가격 * 수량이 10,000 보다 작으면
            if (resultPrice < 10000) {
                // 특정 필드랑 비교하기 어려움 -> global 오류
                bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
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

//    @PostMapping("/add")
    public String addItemV2(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        // BindingResult : 스프링이 제공하는 검증 오류를 보관하는 객체
        // @ModelAttribute 는 자동으로 model 에 등록함.

        // 검증 로직

        // 필드 오류 검증
        // 글자가 없으면
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false, null, null, "상품 이름은 필수입니다."));
        }
        // 가격이 없고 1,000 ~ 1,000,000 이 아니면
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false, null, null,"가격은 1,000 ~ 1,000,000 까지 허용합니다."));
        }
        // 수량이 없고 9,999 이상이면
        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false, null, null, "수량은 최대 9,999 까지 허용합니다."));
        }

        // 특정 필드가 아닌 복합 룰 검증
        // 가격이 있으면서 수량도 있으면
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            // 가격 * 수량이 10,000 보다 작으면
            if (resultPrice < 10000) {
                // 특정 필드랑 비교하기 어려움 -> global 오류
                bindingResult.addError(new ObjectError("item", null, null, "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
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

//    @PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        // BindingResult : 스프링이 제공하는 검증 오류를 보관하는 객체
        // @ModelAttribute 는 자동으로 model 에 등록함.

        // 검증 로직

        // 필드 오류 검증
        // 글자가 없으면
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false, new String[]{"required.item.itemName"}, null, null));
        }
        // 가격이 없고 1,000 ~ 1,000,000 이 아니면
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false, new String[]{"range.item.price"}, new Object[]{1000, 1000000},null));
        }
        // 수량이 없고 9,999 이상이면
        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false, new String[]{"max.item.quantity"}, new Object[]{9999}, null));
        }

        // 특정 필드가 아닌 복합 룰 검증
        // 가격이 있으면서 수량도 있으면
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            // 가격 * 수량이 10,000 보다 작으면
            if (resultPrice < 10000) {
                // 특정 필드랑 비교하기 어려움 -> global 오류
                bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice}, null));
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

//    @PostMapping("/add")
    public String addItemV4(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        // BindingResult : 스프링이 제공하는 검증 오류를 보관하는 객체
        // @ModelAttribute 는 자동으로 model 에 등록함.

        // 검증 로직

        // 필드 오류 검증
        // 글자가 없으면
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.rejectValue("itemName", "required");
        }
        // 가격이 없고 1,000 ~ 1,000,000 이 아니면
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }
        // 수량이 없고 9,999 이상이면
        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            bindingResult.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

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

//    @PostMapping("/add")
    public String addItemV5(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        // BindingResult : 스프링이 제공하는 검증 오류를 보관하는 객체
        // @ModelAttribute 는 자동으로 model 에 등록함.

        // 검증
        itemValidator.validate(item, bindingResult);

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

    @PostMapping("/add")
    public String addItemV6(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        // BindingResult : 스프링이 제공하는 검증 오류를 보관하는 객체
        // @ModelAttribute 는 자동으로 model 에 등록함.
        // @Validated 는 Item 에 대해서 검증을 수행한다.
        // 검증을 하고 bindResult 에 결과 값이 담긴다.

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
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v3/items/{itemId}";
    }

}

