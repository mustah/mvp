import * as React from 'react';
import {connectedAdminOnly} from '../../../components/hoc/withRoles';
import {ThemeContext, withCssStyles} from '../../../components/hoc/withThemeProvider';
import {Row, RowRight, RowSpaceBetween} from '../../../components/layouts/row/Row';
import {WithChildren} from '../../../types/Types';
import {NotificationsContainer} from '../containers/NotificationsContainer';
import {ProfileContainer} from '../containers/ProfileContainer';
import {AppSwitch} from './AppSwitch';
import './TopMenu.scss';

const AppSwitchDropdownComponent = connectedAdminOnly(AppSwitch);

export const TopMenu = withCssStyles(({children, cssStyles: {primary}}: WithChildren & ThemeContext) => (
  <RowSpaceBetween className="TopMenu" style={{backgroundColor: primary.bgDark}}>
    <Row className="TopMenu-LeftContent">
      {children}
    </Row>
    <RowRight className="TopMenu-RightContent">
      <NotificationsContainer/>
      <AppSwitchDropdownComponent/>
      <ProfileContainer/>
    </RowRight>
  </RowSpaceBetween>
));
