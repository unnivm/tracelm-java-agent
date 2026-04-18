/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.usbtechno.collector.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignupRequest {

    @NotBlank(message = "name is required")
    @Size(max = 120, message = "name must be 120 characters or fewer")
    public String name;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    @Size(max = 190, message = "email must be 190 characters or fewer")
    public String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 128, message = "password must be between 8 and 128 characters")
    public String password;
}
