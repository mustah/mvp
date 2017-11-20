import * as classNames from 'classnames';
import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {bindActionCreators} from 'redux';
import {routes} from '../../../../app/routes';
import {Row, RowCenter} from '../../../../components/layouts/row/Row';
import {Logo} from '../../../../components/logo/Logo';
import {RootState} from '../../../../reducers/rootReducer';
import {ClassNamed} from '../../../../types/Types';
import {logout} from '../../../auth/authActions';
import {AuthState} from '../../../auth/authReducer';
import {Profile} from '../profile/Profile';
import './SelectionMenuWrapper.scss';

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
      <RowCenter>
        <Link className="Logo" to={routes.home}>
          <Logo className="small"/>
        </Link>
      </RowCenter>
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
