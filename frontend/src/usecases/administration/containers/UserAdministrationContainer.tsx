import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {UserActionsDropdown} from '../../../components/actions-dropdown/UserActionsDropdown';
import {UsersActionsDropdown} from '../../../components/actions-dropdown/UsersActionsDropdown';
import {DeleteUserAlert} from '../../../components/dialog/DeleteUserDialog';
import {Column} from '../../../components/layouts/column/Column';
import {RowRight} from '../../../components/layouts/row/Row';
import {Loader} from '../../../components/loading/Loader';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {ClearError, ObjectsById} from '../../../state/domain-models/domainModels';
import {clearErrorUsers, deleteUser, fetchUsers} from '../../../state/domain-models/domainModelsActions';
import {getError} from '../../../state/domain-models/domainModelsSelectors';
import {filterUsersByUser, User} from '../../../state/domain-models/user/userModels';
import {getUserEntities} from '../../../state/domain-models/user/userSelectors';
import {ErrorResponse, OnClickWithId, uuid} from '../../../types/Types';

interface StateToProps {
  currentUser: User;
  users: ObjectsById<User>;
  encodedUriParametersForUsers: string;
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
}

interface DispatchToProps {
  deleteUser: OnClickWithId;
  fetchUsers: (encodedUriParameters: string) => void;
  clearError: ClearError;
}

type Props = StateToProps & DispatchToProps;

interface State {
  isDeleteDialogOpen: boolean;
  userToDelete?: uuid;
}

class UserAdministration extends React.Component<Props, State> {

  state: State = {isDeleteDialogOpen: false};

  componentDidMount() {
    const {fetchUsers, encodedUriParametersForUsers} = this.props;
    fetchUsers(encodedUriParametersForUsers);
  }

  componentWillReceiveProps({fetchUsers, encodedUriParametersForUsers}: Props) {
    fetchUsers(encodedUriParametersForUsers);
  }

  openDialog = (id: uuid) => this.setState({isDeleteDialogOpen: true, userToDelete: id});
  closeDialog = () => this.setState({isDeleteDialogOpen: false});

  deleteSelectedUser = () => this.props.deleteUser(this.state.userToDelete!);

  render() {
    const {
      currentUser,
      users,
      isFetching,
      error,
      clearError,
    } = this.props;

    const renderName = ({name}: User) => name;
    const renderEmail = ({email}: User) => email;
    const renderOrganisation = ({organisation: {name}}: User) => name;
    const renderRoles = ({roles}: User) => roles.join(', ');
    const renderActionDropdown = ({id}: User) =>
      <UserActionsDropdown confirmDelete={this.openDialog} id={id}/>;

    // TODO filter the companies in the backend instead, to get rid of this manipulation in the front end
    const usersToRender = filterUsersByUser(users, currentUser);
    const paginatedList = Object.keys(usersToRender);

    return (
      <Loader isFetching={isFetching} error={error} clearError={clearError}>
        <Column>
          <RowRight>
            <UsersActionsDropdown/>
          </RowRight>
          <Table result={paginatedList} entities={usersToRender}>
            <TableColumn
              header={<TableHead className="first">{translate('name')}</TableHead>}
              renderCell={renderName}
            />
            <TableColumn
              header={<TableHead>{translate('email')}</TableHead>}
              renderCell={renderEmail}
            />
            <TableColumn
              header={<TableHead>{translate('organisation')}</TableHead>}
              renderCell={renderOrganisation}
            />
            <TableColumn
              header={<TableHead>{translate('roles')}</TableHead>}
              renderCell={renderRoles}
            />
            <TableColumn
              header={<TableHead className="actionDropdown">{' '}</TableHead>}
              renderCell={renderActionDropdown}
            />
          </Table>
          <DeleteUserAlert
            isOpen={this.state.isDeleteDialogOpen}
            close={this.closeDialog}
            confirm={this.deleteSelectedUser}
          />
        </Column>
      </Loader>
    );
  }
}

const mapStateToProps = ({domainModels: {users}, auth}: RootState): StateToProps => {
  return {
    users: getUserEntities(users),
    currentUser: auth.user!,
    encodedUriParametersForUsers: '',
    isFetching: users.isFetching,
    error: getError(users),
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  deleteUser,
  fetchUsers,
  clearError: clearErrorUsers,
}, dispatch);

export const UserAdministrationContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(UserAdministration);
