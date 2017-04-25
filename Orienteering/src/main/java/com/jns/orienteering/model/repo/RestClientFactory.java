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
package com.jns.orienteering.model.repo;

import java.util.Arrays;
import java.util.List;

import com.gluonhq.connect.provider.RestClient;

class RestClientFactory {

    private static final String   APP_ID          = "https://orienteering-2dd97.firebaseio.com";
    private static final String   AUTH_PARAM_NAME = "auth";
    private static final String   CREDENTIALS     = "2ekET9SyGxrYCeSWgPZaWdiCHxncCHmAvGCjDjwu";

    protected static final String GET             = "GET";
    protected static final String PUT             = "PUT";
    protected static final String POST            = "POST";

    private RestClientFactory() {
    }

    static RestClient baseClient() {
        RestClient client = RestClient.create().host(APP_ID);
        client.queryParam(AUTH_PARAM_NAME, CREDENTIALS);
        // client.queryParam("print", "pretty");
        return client;
    }

    static RestClient queryClient(String url) {
        return create(GET, url, QueryParameter.shallow());
    }

    static RestClient queryClient(List<QueryParameter> queryParams, String url) {
        return create(GET, url, queryParams);
    }

    static RestClient deleteClient(String url) {
        return create(POST, url, QueryParameter.deleteOverride(), QueryParameter.shallow());
    }

    static RestClient create(String method, String url, QueryParameter... queryParameters) {
        return create(method, url, Arrays.asList(queryParameters));
    }

    static RestClient create(String method, String url, List<QueryParameter> queryParameters) {
        RestClient client = baseClient();
        client.method(method);
        client.path(url);
        for (QueryParameter param : queryParameters) {
            client.queryParam(param.getKey(), param.getValue());
        }
        return client;
    }
}
