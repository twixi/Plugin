/*
 * Tables.java
 * 
 * Statistics
 * Copyright (C) 2013 bitWolfy <http://www.wolvencraft.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.mctrakr.modules.stats.distance;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import com.mctrakr.database.Column;

public class Tables {
    
    private Tables() { }
    
    /**
     * Represents the <i>distances</i> table.
     * @author bitWolfy
     *
     */
    @AllArgsConstructor(access=AccessLevel.PUBLIC)
    @Getter(AccessLevel.PUBLIC)
    public enum DistancesTable implements Column {
        TableName       ("distances"),
        PlayerId        ("player_id"),
        Foot            ("foot"),
        Swim            ("swim"),
        Flight          ("flight"),
        Boat            ("boat"),
        Minecart        ("minecart"),
        Ride            ("ride");
        
        private String columnName;
        
        @Override
        public String toString() { return columnName; }
    }
    
}
