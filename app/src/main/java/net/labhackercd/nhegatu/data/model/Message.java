/*
 * This file is part of Nhegatu, the e-Demoracia Client for Android.
 *
 * Nhegatu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nhegatu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nhegatu.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.labhackercd.nhegatu.data.model;

import java.util.Date;
import java.util.UUID;

public interface Message {
    UUID getUuid();
    long getUserId();
    long getGroupId();
    long getCategoryId();
    long getThreadId();
    long getMessageId();
    long getRootMessageId();
    long getParentMessageId();
    String getBody();
    String getSubject();
    Date getCreateDate();
}