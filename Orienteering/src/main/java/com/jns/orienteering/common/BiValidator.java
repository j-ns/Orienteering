/*
 *
 *  Copyright 2016 - 2017, Jens Stroh
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL JENS STROH BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jns.orienteering.common;

import static com.jns.orienteering.control.Dialogs.showError;
import static com.jns.orienteering.util.Validations.isNullOrEmpty;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class BiValidator<T, U> implements Validator {

    private Supplier<T>               supplier1;
    private Supplier<U>               supplier2;
    private BiFunction<T, U, Boolean> predicateFunction;
    private String                    errorMessage;

    public BiValidator(BiFunction<T, U, Boolean> predicateFunction, String errorMessage) {
        this(null, null, predicateFunction, errorMessage);
    }

    public BiValidator(Supplier<T> supplier1, Supplier<U> supplier2, BiFunction<T, U, Boolean> predicateFunction, String errorMessage) {
        this.supplier1 = supplier1;
        this.supplier2 = supplier2;
        this.predicateFunction = predicateFunction;
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean check() {
        return check(supplier1.get(), supplier2.get());
    }

    public boolean check(T t, U u) {
        if (!predicateFunction.apply(t, u)) {
            if (!isNullOrEmpty(errorMessage)) {
                showError(errorMessage);
            }
            return false;
        }
        return true;
    }

}
