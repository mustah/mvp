import {Grid, GridColumn} from '@progress/kendo-react-grid';
import {toArray} from 'lodash';
import * as React from 'react';
import {useConfirmDialog} from '../../../components/dialog/confirmDialogHook';
import {ConfirmDialog} from '../../../components/dialog/DeleteConfirmDialog';
import {Column} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {RetryLoader} from '../../../components/loading/Loader';
import {translate} from '../../../services/translationService';
import {DispatchToProps, StateToProps} from '../containers/OrganisationsContainer';
import {AddOrganisationButton} from './AddOrganisationButton';
import {OrganisationActions} from './OrganisationActions';

type Props = StateToProps & DispatchToProps;

export const OrganisationList = ({
  deleteOrganisation,
  clearError,
  error,
  fetchOrganisations,
  isFetching,
  organisations: {entities},
}: Props) => {
  React.useEffect(() => {
    fetchOrganisations();
  });
  const {isOpen, openConfirm, closeConfirm, confirm} = useConfirmDialog(deleteOrganisation);

  const parent = ({dataItem}) => <td>{dataItem.parent ? dataItem.parent.name : '-'}</td>;
  const actions = ({dataItem: {id}}) => <td><OrganisationActions confirmDelete={openConfirm} id={id}/></td>;

  return (
    <RetryLoader isFetching={isFetching} error={error} clearError={clearError}>
      <Column>
        <Row>
          <AddOrganisationButton/>
        </Row>
        <Grid style={{borderTopWidth: 1}} data={toArray(entities)} scrollable="none">
          <GridColumn field="name" title={translate('name')} headerClassName="left-most" className="left-most"/>
          <GridColumn cell={parent} title={translate('parent organisation')}/>
          <GridColumn field="slug" title={translate('slug')}/>
          <GridColumn cell={actions} width={40}/>
        </Grid>
        <ConfirmDialog isOpen={isOpen} close={closeConfirm} confirm={confirm}/>
      </Column>
    </RetryLoader>
  );
};
