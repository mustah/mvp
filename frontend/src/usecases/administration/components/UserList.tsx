import {Grid, GridColumn} from '@progress/kendo-react-grid';
import {toArray} from 'lodash';
import * as React from 'react';
import {useConfirmDialog} from '../../../components/dialog/confirmDialogHook';
import {ConfirmDialog} from '../../../components/dialog/DeleteConfirmDialog';
import {Column} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {Loader} from '../../../components/loading/Loader';
import {translate} from '../../../services/translationService';
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
  users: {entities},
}: Props) => {
  React.useEffect(() => {
    fetchUsers();
  });
  const {isOpen, openConfirm, closeConfirm, confirm} = useConfirmDialog(deleteUser);

  const roles = ({dataItem: {roles}}) => <td>{roles.join(', ')}</td>;
  const actions = ({dataItem: {id}}) => <td><UserActions confirmDelete={openConfirm} id={id}/></td>;

  return (
    <Loader isFetching={isFetching} error={error} clearError={clearError}>
      <Column>
        <Row>
          <AddUserButton/>
        </Row>
        <Grid style={{borderTopWidth: 1}} data={toArray(entities)} scrollable="none">
          <GridColumn field="name" title={translate('name')} headerClassName="left-most" className="left-most"/>
          <GridColumn field="email" title={translate('email')}/>
          <GridColumn field="organisation.name" title={translate('organisation')}/>
          <GridColumn cell={roles} title={translate('roles')}/>
          <GridColumn cell={actions} width={40}/>
        </Grid>
        <ConfirmDialog isOpen={isOpen} close={closeConfirm} confirm={confirm}/>
      </Column>
    </Loader>
  );
};
