package org.web.codefm.domain.session;

import static java.util.Arrays.stream;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Data;
import lombok.Generated;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Data
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
@Generated
public class SessionUser implements Serializable {

  private String id;

  private String username;

  private String email;

  private List<String> roles;

  private List<String> permisos;

  private Map<String, String> parameters = new HashMap<>();

  private Locale locale;

  /**
   * Retrieves a parameter value and converts it to the specified type. Supports primitive types, String, and List collections.
   *
   * @param parameter the session parameter to retrieve
   * @param type the class type to convert the value to
   * @param <T> the generic type
   * @return the parameter value converted to the specified type, or null if not found
   * @throws IllegalArgumentException if the parameter cannot be converted to the specified type
   *     <p>
   *     Examples: - getParameter(SessionParameter.TEACHER_ID, Integer.Class) -> returns Integer - getParameter(SessionParameter.ROLES,
   *     List.Class) -> returns List<String> (comma-separated values split)
   *
   *     Note: For List type, use getParameterAsList() to specify the element type explicitly
   */
  public <T> T getParameter(SessionParameter parameter, Class<T> type) {
    try {
      String value = parameters.get(parameter.getClaimName());
      if (value == null) {
        return null;
      }
      return convertValue(value, type);
    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot convert parameter " + parameter.getClaimName() + " to type " + type.getSimpleName(), e);
    }
  }

  /**
   * Retrieves a parameter value as a List and converts each element to the specified element type. Supports List<String>, List<Integer>,
   * List<Long>, List<Boolean>, List<Double>, List<Float>.
   *
   * @param parameter the session parameter to retrieve
   * @param elementType the class type of list elements
   * @param <T> the generic type for list elements
   * @return the parameter value converted to List<T>, or null if not found
   * @throws IllegalArgumentException if elements cannot be converted to the specified type
   *     <p>
   *     Examples: - getParameterAsList(SessionParameter.ROLES, String.Class) -> returns List<String> -
   *     getParameterAsList(SessionParameter.IDS, Integer.Class) -> returns List<Integer>
   */
  public <T> List<T> getParameterAsList(SessionParameter parameter, Class<T> elementType) {
    try {
      String value = parameters.get(parameter.getClaimName());
      if (value == null) {
        return Collections.emptyList();
      }
      String[] elements = value.split(",");
      return stream(elements).map(String::trim)
          .map(element -> convertValue(element, elementType)).toList();
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Cannot convert list elements from parameter " + parameter.getClaimName() + " to type " + elementType.getSimpleName(), e);
    }
  }

  private <T> T convertValue(String value, Class<T> type) {
    return switch (type.getSimpleName()) {
      case "String" -> type.cast(value);
      case "Integer" -> type.cast(Integer.parseInt(value));
      case "Long" -> type.cast(Long.parseLong(value));
      case "Boolean" -> type.cast(Boolean.parseBoolean(value));
      case "Double" -> type.cast(Double.parseDouble(value));
      case "Float" -> type.cast(Float.parseFloat(value));
      default -> throw new IllegalArgumentException("Unsupported type: " + type.getName());
    };
  }
}
