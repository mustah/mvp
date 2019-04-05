import {default as classNames} from 'classnames';
import * as React from 'react';
import {connectedAdminOnly} from '../../../components/hoc/withRoles';
import {Row, RowRight, RowSpaceBetween} from '../../../components/layouts/row/Row';
import {ClassNamed, WithChildren} from '../../../types/Types';
import {NotificationsContainer} from '../containers/NotificationsContainer';
import {ProfileContainer} from '../containers/ProfileContainer';
import {AppSwitch} from './AppSwitch';
import './TopMenu.scss';

const AppSwitchDropdownComponent = connectedAdminOnly(AppSwitch);

export const TopMenu = ({children, className}: ClassNamed & WithChildren) => (
  <RowSpaceBetween className={classNames('TopMenu', className)}>
    <Row className="SelectionMenu">
      {children}
    </Row>
    <RowRight className="TopMenu-RightContent">
      <NotificationsContainer/>
      <AppSwitchDropdownComponent/>
      <ProfileContainer/>
    </RowRight>
  </RowSpaceBetween>
);
