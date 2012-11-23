/**
 * jquery.unique-element-id.js
 *
 * A simple jQuery plugin to get a unique ID for
 * any HTML element
 *
 * Usage:
 *    $('some_element_selector').uid();
 *
 * by Jamie Rumbelow <jamie@jamierumbelow.net>
 * http://jamieonsoftware.com
 * Copyright (c)2011 Jamie Rumbelow
 *
 * Licensed under the MIT license (http://www.opensource.org/licenses/MIT)
 */

(function($){
  var __uid_counter = 0;
  
  /**
   * Generate a new unqiue ID
   */
  function generateUniqueId() {
    // Increment the counter
    __uid_counter++;
    
    // Return a unique ID
    return "element-" + __uid_counter;
  }
  
  /**
   * Get a unique ID for an element, ensuring that the
   * element has an id="" attribute
   */
  $.fn.uid = function(){
    // We need an element! Check the selector returned something
    if (!this.length > 0) {
      return false;
    }
    
    // Act on only the first element. Also, fetch the element's ID attr
    var first_element = this.first(),
        id_attr = first_element.attr('id');
        
    // Do we have an ID?
    if (!id_attr) {
      // No? Generate one!
      id_attr = generateUniqueId();
      
      // And set the ID attribute
      first_element.attr('id', id_attr);
    }
    
    // Return it
    return id_attr;
  };
})(jQuery);
