/*
 * Copyright 2011 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.data.validator;

/**
 * String validator for integers. See
 * {@link com.vaadin.data.validator.AbstractStringValidator} for more
 * information.
 * 
 * @author Vaadin Ltd.
 * @version
 * 6.8.13
 * @since 5.4
 */
@SuppressWarnings("serial")
public class IntegerValidator extends AbstractStringValidator {

    /**
     * Creates a validator for checking that a string can be parsed as an
     * integer.
     * 
     * @param errorMessage
     *            the message to display in case the value does not validate.
     */
    public IntegerValidator(String errorMessage) {
        super(errorMessage);

    }

    @Override
    protected boolean isValidString(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
