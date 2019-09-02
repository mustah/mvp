import {Grid, GridColumn} from '@progress/kendo-react-grid';
import {sortBy, toArray} from 'lodash';
import * as React from 'react';
import {routes} from '../../../app/routes';
import {makeGridClassName} from '../../../app/themes';
import {useConfirmDialog} from '../../../components/dialog/confirmDialogHook';
import {ConfirmDialog} from '../../../components/dialog/DeleteConfirmDialog';
import {ThemeContext} from '../../../components/hoc/withThemeProvider';
import {Column} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {RetryLoader} from '../../../components/loading/Loader';
import {translate} from '../../../services/translationService';
import {User} from '../../../state/domain-models/user/userModels';
import {UseCases} from '../../../types/Types';
import {DispatchToProps, StateToProps} from '../containers/UsersContainer';
import {AddUserButton} from './AddUserButton';
import {UserActions} from './UserActions';

export interface OwnProps {
  useCase: UseCases;
}

type Props = StateToProps & DispatchToProps & OwnProps & ThemeContext;

export const UserList = ({
  cssStyles,
  clearError,
  deleteUser,
  error,
  fetchUsers,
  isFetching,
  useCase,
  users: {entities},
}: Props) => {
  React.useEffect(() => {
    fetchUsers();
  });
  const {isOpen, openConfirm, closeConfirm, confirm} = useConfirmDialog(deleteUser);

  const roles = ({dataItem: {roles}}) => <td>{roles.join(', ')}</td>;
  const actions = ({dataItem: {id}}) => <td><UserActions confirmDelete={openConfirm} id={id} useCase={useCase}/></td>;

  return (
    <RetryLoader isFetching={isFetching} error={error} clearError={clearError}>
      <Column>
        <Row>
          <AddUserButton linkTo={useCase === UseCases.otc ? routes.otcUsersAdd : routes.adminUsersAdd}/>
        </Row>
        <Grid
          className={makeGridClassName(cssStyles)}
          style={{borderTopWidth: 1}}
          data={sortBy(toArray(entities), (u: User) => u.name)}
          scrollable="none"
        >
          <GridColumn field="name" title={translate('name')} headerClassName="left-most" className="left-most"/>
          <GridColumn field="email" title={translate('email')}/>
          <GridColumn field="organisation.name" title={translate('organisation')}/>
          <GridColumn cell={roles} title={translate('roles')}/>
          <GridColumn cell={actions} width={40}/>
        </Grid>
        <ConfirmDialog isOpen={isOpen} close={closeConfirm} confirm={confirm}/>
      </Column>
    </RetryLoader>
  );
};
