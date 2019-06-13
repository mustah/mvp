import {Grid, GridColumn} from '@progress/kendo-react-grid';
import * as React from 'react';
import {makeGridClassName} from '../../../app/themes';
import {useConfirmDialog} from '../../../components/dialog/confirmDialogHook';
import {ThemeContext} from '../../../components/hoc/withThemeProvider';
import {Column} from '../../../components/layouts/column/Column';
import {RetryLoader} from '../../../components/loading/Loader';
import {translate} from '../../../services/translationService';
import {OrganisationConfirmContainer} from '../containers/OrganisationConfirmContainer';
import {DispatchToProps, StateToProps} from '../containers/OrganisationsContainer';
import {AddOrganisationButton} from './AddOrganisationButton';
import {OrganisationActions} from './OrganisationActions';

type Props = StateToProps & DispatchToProps & ThemeContext;

export const OrganisationList = ({
  clearError,
  cssStyles,
  deleteOrganisation,
  error,
  fetchOrganisations,
  isFetching,
  organisations,
  syncMetersOrganisation,
}: Props) => {
  React.useEffect(() => {
    fetchOrganisations();
  });

  const {isOpen, openConfirm, id: organisationId, closeConfirm, confirm} = useConfirmDialog(deleteOrganisation);

  const parent = ({dataItem}) => <td>{dataItem.parent ? dataItem.parent.name : '-'}</td>;

  const actions = ({dataItem: {id, name}}) => (
    <td>
      <OrganisationActions confirmDelete={openConfirm} id={id} syncMetersOrganisation={syncMetersOrganisation}/>
      <OrganisationConfirmContainer
        isOpen={isOpen && id === organisationId}
        close={closeConfirm}
        confirm={confirm}
        idName={{id, name}}
      />
    </td>
  );

  return (
    <RetryLoader isFetching={isFetching} error={error} clearError={clearError}>
      <Column>
        <AddOrganisationButton/>
        <Grid
          className={makeGridClassName(cssStyles)}
          style={{borderTopWidth: 1}}
          data={organisations}
          scrollable="none"
        >
          <GridColumn field="name" title={translate('name')} headerClassName="left-most" className="left-most"/>
          <GridColumn cell={parent} title={translate('parent organisation')}/>
          <GridColumn field="slug" title={translate('slug')}/>
          <GridColumn cell={actions} width={40}/>
        </Grid>
      </Column>
    </RetryLoader>
  );
};
