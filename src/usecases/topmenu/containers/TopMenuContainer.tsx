import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../app/routes';
import {Row} from '../../layouts/components/row/Row';
import {ProfileContainer} from '../../profile/containers/ProfileContainer';
import {MainLogo} from '../components/mainlogo/MainLogo';
import {MenuItem} from '../components/menuitems/MenuItem';

export const TopMenuContainer = props => (
  <Row className="flex-1">
    <MainLogo/>
    <Row>
      <Link to={routes.dashboard} className="link">
        <MenuItem name="Dashboard" isSelected={true} icon="dialpad"/>
      </Link>
      <Link to={routes.collection} className="link">
        <MenuItem name="Insamling" isSelected={false} icon="dialpad"/>
      </Link>
      <Link to={routes.validation} className="link">
        <MenuItem name="Validering" isSelected={false} icon="dialpad"/>
      </Link>
      <Link to={routes.dataAnalysis} className="link">
        <MenuItem name="Mätserier" isSelected={false} icon="dialpad"/>
      </Link>
    </Row>
    <ProfileContainer/>
  </Row>
);
