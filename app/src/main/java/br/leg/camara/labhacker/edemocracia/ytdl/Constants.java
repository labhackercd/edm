 /*
 * Copyright (c) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package br.leg.camara.labhacker.edemocracia.ytdl;

/**
 * @author Ibrahim Ulukaya <ulukaya@google.com>
 *         <p/>
 *         This class hold constants.
 */
public class Constants {
    public static final int MAX_KEYWORD_LENGTH = 30;
    public static final String DEFAULT_KEYWORD = "e-democracia app";
    // A playlist ID is a string that begins with PL. You must replace this string with the correct
    // playlist ID for the app to work
    public static final String UPLOAD_PLAYLIST = "PLdXJWWDmjGDJEJDArbcTKGEu41JvWLyTP";
    public static final String APP_NAME = "edemocracia";
    public static final String ACCOUNT_KEY = "ACCOUNT";
    public static final String YOUTUBE_WATCH_URL_PREFIX = "watch?v=";

    public static final String REQUEST_AUTHORIZATION_INTENT = "br.leg.camara.labhacker.edemocracia.RequestAuth";
    public static final String REQUEST_AUTHORIZATION_INTENT_PARAM = "br.leg.camara.labhacker.edemocracia.RequestAuth.param";
}
