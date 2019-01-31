import * as React from 'react';
import {useConfirmDialog} from '../../../components/dialog/confirmDialogHook';
import {ConfirmDialog} from '../../../components/dialog/DeleteConfirmDialog';
import {Column} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {Loader} from '../../../components/loading/Loader';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
import {translate} from '../../../services/translationService';
import {User} from '../../../state/domain-models/user/userModels';
import {DispatchToProps, StateToProps} from '../containers/UsersContainer';
import {AddUserButton} from './AddUserButton';
import {UserActions} from './UserActions';

type Props = StateToProps & DispatchToProps;

export const UserList = ({
  isFetching,
  clearError,
  deleteUser,
  error,
  fetchUsers,
  users,
}: Props) => {
  React.useEffect(() => {
    fetchUsers();
  });
  const {isOpen, openConfirm, closeConfirm, confirm} = useConfirmDialog(deleteUser);

  const renderName = ({name}: User) => name;
  const renderEmail = ({email}: User) => email;
  const renderOrganisation = ({organisation: {name}}: User) => name;
  const renderRoles = ({roles}: User) => roles.join(', ');
  const renderActionDropdown = ({id}: User) => <UserActions confirmDelete={openConfirm} id={id}/>;

  return (
    <Loader isFetching={isFetching} error={error} clearError={clearError}>
      <Column>
        <Row>
          <AddUserButton/>
        </Row>
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
        <ConfirmDialog isOpen={isOpen} close={closeConfirm} confirm={confirm}/>
      </Column>
    </Loader>
  );
};
