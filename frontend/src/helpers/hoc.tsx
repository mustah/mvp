import * as React from 'react';
import {connect} from 'react-redux';
import {RowCenter} from '../components/layouts/row/Row';
import {LoadingLarge} from '../components/loading/Loading';
import {RootState} from '../reducers/rootReducer';
import {Role, User} from '../state/domain-models/user/userModels';
import {getUser} from '../usecases/auth/authSelectors';

interface AuthenticatedUser {
  user: User;
}

export const isSuperAdmin = (user: User): boolean => user.roles.includes(Role.SUPER_ADMIN);

const isAdmin = (user: User): boolean => user.roles.includes(Role.ADMIN) || isSuperAdmin(user);

/**
 * This is a simple implementation of a Higher Order Component (HOC).
 *
 * Only render this component if the user is a super admin. Otherwise return null and do not render.
 */
const whenRole = <P extends AuthenticatedUser>(predicate: (user: User) => boolean) =>
  (Component: React.ComponentType<P>): React.SFC<P> =>
    (props: P) => predicate(props.user) ? <Component {...props} /> : null;

const onlyAdmins = <P extends AuthenticatedUser>(Component: React.ComponentType<P>) => {
  const AdminComponent: React.SFC<P> = whenRole<P>(isAdmin)(Component);
  AdminComponent.displayName = `Admin(${Component.name})`;
  return AdminComponent;
};

export const onlySuperAdmins = <P extends AuthenticatedUser>(Component: React.ComponentType<P>) => {
  const SuperAdminComponent: React.SFC<P> = whenRole<P>(isSuperAdmin)(Component);
  SuperAdminComponent.displayName = `SuperAdmin(${Component.name})`;
  return SuperAdminComponent;
};

const mapStateToProps = ({auth}: RootState): AuthenticatedUser => ({
  user: getUser(auth),
});

export const superAdminComponent =
  <OwnProps extends {}>(Component: React.ComponentType<OwnProps & AuthenticatedUser>) =>
    connect<AuthenticatedUser>(mapStateToProps)(onlySuperAdmins(Component));

export const adminComponent =
  <OwnProps extends {}>(Component: React.ComponentType<OwnProps & AuthenticatedUser>) =>
    connect<AuthenticatedUser>(mapStateToProps)(onlyAdmins(Component));

interface Fetching {
  isFetching: boolean;
}

export const withLargeLoader =
  <P extends Fetching>(Component: React.ComponentType<P>): React.SFC<P> =>
    (props: P) => props.isFetching
      ? (<RowCenter><LoadingLarge/></RowCenter>)
      : (<Component {...props}/>);
