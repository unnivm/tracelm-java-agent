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

package org.usbtechno.collector.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.usbtechno.collector.domain.UserAccount;

import java.util.Optional;

@ApplicationScoped
public class UserAccountRepository implements PanacheRepositoryBase<UserAccount, Long> {

    public Optional<UserAccount> findByEmail(String email) {
        return find("email", email == null ? null : email.trim().toLowerCase()).firstResultOptional();
    }
}
