/**
 *
 *  Copyright (c) 2016, Jens Stroh
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
package com.jns.orienteering.model.repo.readerwriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.gluonhq.connect.converter.JsonOutputConverter;
import com.jns.orienteering.model.common.BaseModel;
import com.jns.orienteering.util.Validators;

public class FileTreeIterableOutputConverter<E extends BaseModel> {

    private static final String    JSON = ".json";

    private File                   root;
    private List<E>                items;
    private JsonOutputConverter<E> converter;

    public FileTreeIterableOutputConverter(Class<E> targetClass, String dir, List<E> items) {
        root = new File(dir);
        this.items = items;
        converter = new JsonOutputConverter<>(targetClass);
    }

    public void writeObjects() {
        if (Validators.isNullOrEmpty(items)) {
            return;
        }

        if (!root.exists()) {
            root.mkdirs();
        }

        for (E candidate : items) {
            try {
                converter.setOutputStream(new FileOutputStream(new File(root, candidate.getId() + JSON)));
                converter.write(candidate);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
