import * as React from 'react';
import {connect} from 'react-redux';
import {RootState} from '../../reducers/rootReducer';
import {User} from '../../state/domain-models/user/userModels';
import {isAdmin, isSuperAdmin} from '../../state/domain-models/user/userSelectors';
import {getUser} from '../../usecases/auth/authSelectors';

interface AuthenticatedUser {
  user: User;
}

/**
 * This is a simple implementation of a Higher Order Component (HOC).
 *
 * Only render this component if the user is a super admin. Otherwise return null and do not render.
 */
const whenRole =
  <P extends AuthenticatedUser>(predicate: (user: User) => boolean) =>
    (Component: React.ComponentType<P>): React.SFC<P> =>
      (props: P) => predicate(props.user) ? <Component {...props} /> : null;

const onlyAdmins =
  <P extends AuthenticatedUser>(Component: React.ComponentType<P>): React.SFC<P> =>
    whenRole<P>(isAdmin)(Component);

const mapStateToProps = ({auth}: RootState): AuthenticatedUser => ({user: getUser(auth)});

export const onlySuperAdmins =
  <P extends AuthenticatedUser>(Component: React.ComponentType<P>): React.SFC<P> =>
    whenRole<P>(isSuperAdmin)(Component);

export const superAdminOnly =
  <OwnProps extends {}>(Component: React.ComponentType<OwnProps & AuthenticatedUser>) =>
    connect<AuthenticatedUser>(mapStateToProps)(onlySuperAdmins(Component));

export const adminOnly =
  <OwnProps extends {}>(Component: React.ComponentType<OwnProps & AuthenticatedUser>) =>
    connect<AuthenticatedUser>(mapStateToProps)(onlyAdmins(Component));
