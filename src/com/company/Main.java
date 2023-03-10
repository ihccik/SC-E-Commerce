package com.company;

import com.company.balance.Balance;
import com.company.balance.CustomerBalance;
import com.company.balance.GiftCardBalance;
import com.company.category.Category;
import com.company.discount.Discount;
import com.company.order.Order;
import com.company.order.OrderService;
import com.company.order.OrderServiceImpl;

import java.util.*;
import java.util.stream.Collectors;

import static com.company.StaticConstants.*;

public class Main {

  public static void main(String[] args) {

    DataGenerator.createCustomer();
    DataGenerator.createCategory();
    DataGenerator.createProduct();
    DataGenerator.createBalance();
    DataGenerator.createDiscount();

    Scanner scanner = new Scanner(System.in);

    System.out.println("Select Customer:");
    for (int i = 0; i < StaticConstants.CUSTOMER_LIST.size(); i++) {
      System.out.println(
          "Type " + i + " for customer:" + StaticConstants.CUSTOMER_LIST.get(i).getUserName());
    }

    Customer customer = null;
    do {
      try {
        customer = StaticConstants.CUSTOMER_LIST.get(scanner.nextInt());
      } catch (IndexOutOfBoundsException e) {
        System.err.println("Enter valid ID number");
      }
    } while (customer == null);

    Cart cart = new Cart(customer);

    while (true) {

      System.out.println("What would you like to do? Just type id for selection");

      for (int i = 0; i < prepareMenuOptions().length; i++) {
        System.out.println(i + "-" + prepareMenuOptions()[i]);
      }

      int menuSelection = scanner.nextInt();

      switch (menuSelection) {
        case 0: //list categories
          for (Category category : StaticConstants.CATEGORY_LIST) {
            System.out.println(
                "Category Code:" + category.generateCategoryCode() + " category name:"
                    + category.getName());
          }
          break;
        case 1: //list products  //product name, product category name
          try {
            System.out.printf("%-20s %-21s %n", "Product Name ", "Product Category Name");
            for (Product product : StaticConstants.PRODUCT_LIST) {
              System.out.printf("%-20s %-20s %n", product.getName(), product.getCategoryName());
            }
          } catch (Exception e) {
            System.out.println(
                "Product could not printed because category not found for product name:"
                    + e.getMessage().split(",")[1]);
          }
          break;
        case 2: //list discounts
          for (Discount discount : StaticConstants.DISCOUNT_LIST) {
            System.out.println(
                "Discount Name: " + discount.getName() + "discount threshold amount: "
                    + discount.getThresholdAmount());
          }
          break;
        case 3://see balance
          CustomerBalance cBalance = findCustomerBalance(customer.getId());
          GiftCardBalance gBalance = findGiftCardBalance(customer.getId());
          double totalBalance = cBalance.getBalance() + gBalance.getBalance();
          System.out.println("Total Balance:" + totalBalance);
          System.out.println("Customer Balance:" + cBalance.getBalance());
          System.out.println("Gift Card Balance:" + gBalance.getBalance());
          break;
        case 4://add balance
          CustomerBalance customerBalance = findCustomerBalance(customer.getId());
          GiftCardBalance giftBalance = findGiftCardBalance(customer.getId());
          System.out.println(
              "For add balance please type 'A', For send balance from your gift card please type 'S'");
          String selectAddOrSend = new Scanner(System.in).next().toUpperCase();

          switch (selectAddOrSend) {
            case "S":
//                            GiftCard gb=findGiftCardBalance(customer.getId());

              System.out.println("Please enter the recipient's id.");
              System.out.println(findCustomerIdAndName(customer.getId()));
              String idSelection = new Scanner(System.in).nextLine();

              double newGiftCardBalance = 0.0;
              System.out.println("How much would you like to send?");
              double amount = new Scanner(System.in).nextDouble();
              try {

                giftBalance.sendMoney(UUID.fromString(idSelection), amount);
              } catch (Exception e) {
                System.err.println("Id could not find. Please try again!!");
                continue;
              }

              break;
            case "A":
              addBalanceForYourAccount(customer.getId());

          }

          break;
        case 5://place an order

          boolean exit = false;
          Map<Product, Integer> map = new HashMap<>();
          cart.setProductMap(map);
          while (true) {
            System.out.println("Which product would you like to add to your cart?");
            Product.listAllProducts();
            System.out.println("To exit product selection - Type : \"exit\"");

            String productId = scanner.next();

            if (productId.equals("exit")) {
              exit = true;
              break;
            }

            try {
              Product product = findProductById(productId);
              if (!putItemToCartIfStockAvailable(cart, product)) {
                System.out.println("Stock is insufficient. Please try again");
                continue;
              }
            } catch (Exception e) {
              System.out.println("Product does not exist. please try again");
              continue;
            }

            System.out.println(
                "Do you want to add more product. Type Y for adding more, N for exit");
            String decision = scanner.next();
            if (!decision.equals("Y")) {
              break;
            }
          }
          if (exit) {
            break;
          }else {
            System.out.println(
                    "seems there are discount options. Do you want to see and apply to your cart if it is applicable. For no discount type no");
            for (Discount discount : DISCOUNT_LIST) {
              System.out.println(
                      "discount id " + discount.getId() + " discount name: " + discount.getName());
            }
            String discountId = scanner.next();
            if (!discountId.equals("no")) {
              try {
                Discount discount = findDiscountById(discountId);
                if (discount.decideDiscountIsApplicableToCart(cart)) {
                  cart.setDiscountId(discount.getId());
                }
              } catch (Exception e) {
                System.out.println(e.getMessage());
              }

            }

            OrderService orderService = new OrderServiceImpl();
            String result = orderService.placeOrder(cart);
            if (result.equals("Order has been placed successfully")) {
              System.out.println("Order is successful");
              updateProductStock(cart.getProductMap());
              cart.setProductMap(new HashMap<>());
              cart.setDiscountId(null);
            } else {
              System.out.println(result);
            }
            break;
          }


        case 6://See cart
          System.out.println("Your Cart");
          if (cart.getProductMap() != null) {
            if (!cart.getProductMap().keySet().isEmpty()) {
              for (Product product : cart.getProductMap().keySet()) {
                System.out.println(
                    "product name: " + product.getName() + " count: " + cart.getProductMap()
                        .get(product));
              }
            }
          } else {
            System.err.println("There is no product added to cart yet");
          }
          break;
        case 7://see order details
          printOrdersByCustomerId(customer.getId());
          break;
        case 8://see your address
          printAddressByCustomerId(customer);

          break;
        case 9: //add phone number
          System.out.println("Please add your phone number");
          addPhoneNumbers(customer);
          break;
        case 10:
          System.exit(1);

          break;
      }
    }
  }

  private static void addPhoneNumbers(Customer customer) {
    Scanner input = new Scanner(System.in);
    long customerPhoneNumber;
    customerPhoneNumber = input.nextLong();
    String formatted = ("" + customerPhoneNumber).replaceAll("(...)(...)(....)", "($1) $2-$3");
    System.out.println(formatted);

    List<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();

    phoneNumbers.add(new PhoneNumber(customerPhoneNumber));

    customer.setPhoneNumbers(phoneNumbers);

    System.out.println("Your phone number is saved");

    customer.getPhoneNumbers()
        .forEach(phoneNumber -> System.out.println(phoneNumber.getPhoneNumber()));
  }

  private static void addBalanceForYourAccount(UUID id) {
    System.out.println("Which account would you like to add:");
    CustomerBalance customerBalance=findCustomerBalance(id);
    GiftCardBalance giftCardBalance=findGiftCardBalance(id);
    System.out.println("Type 1 for Customer Balance " + customerBalance.getBalance());
    System.out.println("Type 2 for Gift Card Balance " + giftCardBalance.getBalance());
    Scanner scanner = new Scanner(System.in);
    int balanceAccountSelection = scanner.nextInt();
    System.out.println("How much you would like to add?");
    double additionalAmount = scanner.nextDouble();
    if (balanceAccountSelection==1){
      customerBalance.addBalance(additionalAmount);
        System.out.println("New customer balance: " + customerBalance.getBalance());
    }else{
      giftCardBalance.addBalance(additionalAmount);
        System.out.println("New gift card balance: " + giftCardBalance.getBalance());
    }
  }

  private static Map<UUID, String> findCustomerIdAndName(UUID id) {
    Map<UUID, String> mapIdAndName = StaticConstants.CUSTOMER_LIST.stream()
        .collect(Collectors.toMap(Customer::getId, Customer::getUserName));
    List<UUID> uuidList = StaticConstants.CUSTOMER_LIST.stream()
        .map(Customer::getId).collect(Collectors.toList());

    mapIdAndName.remove(id);

    return mapIdAndName;
  }

  private static Discount findDiscountById(String discountId) {
    return DISCOUNT_LIST.stream()
        .filter(i -> i.getId().toString().equals(discountId)).findFirst().orElseThrow(
            () -> new RuntimeException("Discount couldn't applied because couldn't found"));
  }

  private static void updateProductStock(Map<Product, Integer> map) {
    map.keySet().stream().forEach(
        product -> product.setRemainingStock(product.getRemainingStock() - map.get(product)));
  }

  private static void printOrdersByCustomerId(UUID customerId) {
    boolean hasOrders = false;//if customer has orders

    for (Order order : ORDER_LIST) {
      if (order.getCustomerId().toString().equals(customerId.toString())) {
        System.out.println(
            "Order status: " + order.getOrderStatus() + " order amount " + order.getPaidAmount()
                + " order date " + order.getOrderDate());
        hasOrders = true;
      }
    }
    //Printing the message if orders list is empty
    if (!hasOrders) {
      System.out.println("*****************************************");
      System.out.println("**                                     **");
      System.out.println("**      The orders list is empty       **");
      System.out.println("**                                     **");
      System.out.println("*****************************************");
    }
  }

  private static void printAddressByCustomerId(Customer customer) {
//    Stream method implementation

    customer.getAddress().stream().forEach(System.out::println);
    System.out.println();

  }

  private static boolean putItemToCartIfStockAvailable(Cart cart, Product product) {

    System.out.println("Please provide product count:");
    Scanner scanner = new Scanner(System.in);
    int count = scanner.nextInt();

    Integer cartCount = cart.getProductMap().get(product);

    if (cartCount != null && product.getRemainingStock() > cartCount + count) {
      cart.getProductMap().put(product, cartCount + count);
      return true;

    } else if (product.getRemainingStock() >= count) {
      cart.getProductMap().put(product, count);
      return true;
    }
    return false;

  }


  private static Product findProductById (String productId) throws Exception {
    Optional<Product> product = PRODUCT_LIST.stream().filter(p->p.getId().toString().equals(productId)).findFirst();

    if (product.isPresent()) {
      return product.get();
    }else {
      throw new Exception ("Product not found");
    }
  }

  private static CustomerBalance findCustomerBalance(UUID customerId) {
    for (Balance customerBalance : StaticConstants.CUSTOMER_BALANCE_LIST) {
      if (customerBalance.getCustomerId().toString().equals(customerId.toString())) {
        return (CustomerBalance) customerBalance;
      }
    }

    CustomerBalance customerBalance = new CustomerBalance(customerId, 0d);
    StaticConstants.CUSTOMER_BALANCE_LIST.add(customerBalance);

    return customerBalance;
  }

  private static GiftCardBalance findGiftCardBalance(UUID customerId) {
    for (Balance giftCarBalance : StaticConstants.GIFT_CARD_BALANCE_LIST) {
      if (giftCarBalance.getCustomerId().toString().equals(customerId.toString())) {
        return (GiftCardBalance) giftCarBalance;
      }
    }

    GiftCardBalance giftCarBalance = new GiftCardBalance(customerId, 0d);
    StaticConstants.GIFT_CARD_BALANCE_LIST.add(giftCarBalance);

    return giftCarBalance;
  }


  private static String[] prepareMenuOptions() {
    return new String[]{"List Categories", "List Products", "List Discount", "See Balance",
        "Add Balance-Send Balance",
        "Place an order", "See Cart", "See order details", "See your address", "Add phone numbers",
        "Close App"};
  }


}
