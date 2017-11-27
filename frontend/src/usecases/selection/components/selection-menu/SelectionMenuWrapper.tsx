import * as classNames from 'classnames';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {ClassNamed} from '../../../../types/Types';
import {routes} from '../../../../app/routes';
import {AuthState} from '../../../auth/authReducer';
import {Row} from '../../../../components/layouts/row/Row';
import {Logo} from '../../../../components/logo/Logo';
import {Profile} from '../../../profile/components/Profile';
import './SelectionMenuWrapper.scss';
import {RootState} from '../../../../reducers/rootReducer';
import {logout} from '../../../auth/authActions';
import {bindActionCreators} from 'redux';
import {connect} from 'react-redux';

interface StateToProps extends ClassNamed {
  auth: AuthState;
  children?: React.ReactNode;
}

interface DispatchToProps {
  logout: () => void;
}

interface OwnProps {
  className: string;
}

const SearchMenuWrapperComponent = (props: StateToProps & DispatchToProps) => {
  const {children, className, auth, logout} = props;

  return (
    <Row className={classNames('SelectionMenuWrapper', className)}>
      <Row className="SelectionMenu">
        {children}
      </Row>
      <Row>
        <Link className="Logo" to={routes.home}>
          <Logo className="small"/>
        </Link>
      </Row>
      <Row>
        <Profile user={auth.user!} logout={logout}/>
      </Row>
    </Row>
  );
};

const mapStateToProps = ({auth}: RootState): StateToProps => {
  return {
    auth,
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  logout,
}, dispatch);

export const SearchMenuWrapper =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(SearchMenuWrapperComponent);
