import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {RootState} from '../../../reducers/index';
import {routes} from '../../app/routes';
import {Row} from '../../layouts/components/row/Row';
import {ProfileContainer} from '../../profile/containers/ProfileContainer';
import {MainLogo} from '../components/mainlogo/MainLogo';
import {MenuItem} from '../components/menuitems/MenuItem';

export interface TopMenuContainerProps {
  pathname: string;
}

const TopMenuContainer = (props: TopMenuContainerProps) => {
  const {pathname} = props;
  return (
    <Row className="flex-1">
      <MainLogo/>
      <Row>
        <Link to={routes.dashboard} replace={true} className="link">
          <MenuItem name="Dashboard" isSelected={routes.dashboard === pathname || '/' === pathname} icon="dialpad"/>
        </Link>
        <Link to={routes.collection} replace={true} className="link">
          <MenuItem name="Insamling" isSelected={routes.collection === pathname} icon="dialpad"/>
        </Link>
        <Link to={routes.validation} className="link">
          <MenuItem name="Validering" isSelected={routes.validation === pathname} icon="dialpad"/>
        </Link>
        <Link to={routes.dataAnalysis} className="link">
          <MenuItem name="Mätserier" isSelected={routes.dataAnalysis === pathname} icon="dialpad"/>
        </Link>
      </Row>
      <ProfileContainer/>
    </Row>
  );
};

const mapStateToProps = (state: RootState) => {
  return {
    pathname: state.routing.location!.pathname,
  };
};

export default connect(mapStateToProps)(TopMenuContainer);
