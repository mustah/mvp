import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {UserActionsDropdown} from '../../../components/actions-dropdown/UserActionsDropdown';
import {UsersActionsDropdown} from '../../../components/actions-dropdown/UsersActionsDropdown';
import {ConfirmDialog} from '../../../components/dialog/DeleteConfirmDialog';
import {Column} from '../../../components/layouts/column/Column';
import {RowRight} from '../../../components/layouts/row/Row';
import {Loader} from '../../../components/loading/Loader';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {
  clearUserError, deleteUser, fetchUsers,
} from '../../../state/domain-models/user/userApiActions';
import {getDomainModel, getError} from '../../../state/domain-models/domainModelsSelectors';
import {User} from '../../../state/domain-models/user/userModels';
import {ClearError, ErrorResponse, OnClickWithId, Fetch, uuid} from '../../../types/Types';

interface StateToProps {
  users: DomainModel<User>;
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
}

interface DispatchToProps {
  deleteUser: OnClickWithId;
  fetchUsers: Fetch;
  clearError: ClearError;
}

interface State {
  isDeleteDialogOpen: boolean;
  userToDelete?: uuid;
}

type Props = StateToProps & DispatchToProps;

class UserAdministration extends React.Component<Props, State> {

  state: State = {isDeleteDialogOpen: false};

  componentDidMount() {
    this.props.fetchUsers();
  }

  componentWillReceiveProps({fetchUsers}: Props) {
    fetchUsers();
  }

  render() {
    const {
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

    return (
      <Loader isFetching={isFetching} error={error} clearError={clearError}>
        <Column>
          <RowRight>
            <UsersActionsDropdown/>
          </RowRight>
          <Table result={users.result} entities={users.entities}>
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
          <ConfirmDialog
            isOpen={this.state.isDeleteDialogOpen}
            close={this.closeDialog}
            confirm={this.deleteSelectedUser}
          />
        </Column>
      </Loader>
    );
  }

  openDialog = (id: uuid) => this.setState({isDeleteDialogOpen: true, userToDelete: id});

  closeDialog = () => this.setState({isDeleteDialogOpen: false});

  deleteSelectedUser = () => this.props.deleteUser(this.state.userToDelete!);
}

const mapStateToProps = ({domainModels: {users}}: RootState): StateToProps => ({
  users: getDomainModel(users),
  isFetching: users.isFetching,
  error: getError(users),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  deleteUser,
  fetchUsers,
  clearError: clearUserError,
}, dispatch);

export const UserAdministrationContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(UserAdministration);
