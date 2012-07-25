/** jbead - http://www.jbead.ch
    Copyright (C) 2001-2012  Damian Brunold

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.apple.eawt;

import java.util.EventListener;

public interface ApplicationListener extends EventListener {

    void handleAbout(ApplicationEvent event);
    void handleOpenApplication(ApplicationEvent event);
    void handleOpenFile(ApplicationEvent event);
    void handlePreferences(ApplicationEvent event);
    void handlePrintFile(ApplicationEvent event);
    void handleQuit(ApplicationEvent event);
    void handleReOpenApplication(ApplicationEvent event);

}
