import * as React from 'react';
import {useConfirmDialog} from '../../../components/dialog/confirmDialogHook';
import {ConfirmDialog} from '../../../components/dialog/DeleteConfirmDialog';
import {Column} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {Loader} from '../../../components/loading/Loader';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
import {translate} from '../../../services/translationService';
import {Organisation} from '../../../state/domain-models/organisation/organisationModels';
import {DispatchToProps, StateToProps} from '../containers/OrganisationsContainer';
import {AddOrganisationButton} from './AddOrganisationButton';
import {OrganisationActions} from './OrganisationActions';

const renderParentOrganisation = ({parent}: Organisation) => parent ? parent.name : '-';
const renderName = ({name}: Organisation) => name;
const renderSlug = ({slug}: Organisation) => slug;

type Props = StateToProps & DispatchToProps;

export const OrganisationList = ({
  deleteOrganisation,
  clearError,
  error,
  fetchOrganisations,
  isFetching,
  organisations,
}: Props) => {
  React.useEffect(() => {
    fetchOrganisations();
  });
  const {isOpen, openConfirm, closeConfirm, confirm} = useConfirmDialog(deleteOrganisation);

  const renderActionDropdown = ({id}: Organisation) => <OrganisationActions confirmDelete={openConfirm} id={id}/>;

  return (
    <Loader isFetching={isFetching} error={error} clearError={clearError}>
      <Column>
        <Row>
          <AddOrganisationButton/>
        </Row>
        <Table {...organisations}>
          <TableColumn
            header={<TableHead className="first">{translate('name')}</TableHead>}
            renderCell={renderName}
          />
          <TableColumn
            header={<TableHead className="first">{translate('parent organisation')}</TableHead>}
            renderCell={renderParentOrganisation}
          />
          <TableColumn
            header={<TableHead>{translate('slug')}</TableHead>}
            renderCell={renderSlug}
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
