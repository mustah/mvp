import {default as classNames} from 'classnames';
import * as React from 'react';
import {AppSwitchDropdown} from '../../../components/actions-dropdown/AppSwitchDropdown';
import {connectedAdminOnly} from '../../../components/hoc/withRoles';
import {Row, RowRight} from '../../../components/layouts/row/Row';
import {ClassNamed, WithChildren} from '../../../types/Types';
import {ProfileContainer} from '../containers/ProfileContainer';
import './TopMenu.scss';

const AppSwitchDropdownComponent = connectedAdminOnly(AppSwitchDropdown);

export const TopMenu = ({children, className}: ClassNamed & WithChildren) => (
  <Row className={classNames('TopMenu space-between', className)}>
    <Row className="SelectionMenu">
      {children}
    </Row>
    <RowRight className="TopMenu-RightContent">
      <AppSwitchDropdownComponent/>
      <ProfileContainer/>
    </RowRight>
  </Row>
);
