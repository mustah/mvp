import * as React from 'react';
import {Row} from '../../layouts/components/row/Row';
import {ProfileContainer} from '../../profile/containers/ProfileContainer';
import {MainLogo} from '../components/mainlogo/MainLogo';
import {MenuItem} from '../components/menuitems/MenuItem';

export const TopMenuContainer = props => (
  <Row className="flex-1">
    <MainLogo/>
    <Row>
      <MenuItem name="Dashboard" isSelected={true} icon="dialpad"/>
      <MenuItem name="Insamling" isSelected={false} icon="dialpad"/>
      <MenuItem name="Validering" isSelected={false} icon="dialpad"/>
      <MenuItem name="Mätserier" isSelected={false} icon="dialpad"/>
    </Row>
    <ProfileContainer/>
  </Row>
);
