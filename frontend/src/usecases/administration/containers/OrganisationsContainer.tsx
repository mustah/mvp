import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {OrganisationActions} from '../../../components/actions-dropdown/OrganisationActions';
import {OrganisationBatchActions} from '../../../components/actions-dropdown/OrganisationBatchActions';
import {ConfirmDialog} from '../../../components/dialog/DeleteConfirmDialog';
import {Column} from '../../../components/layouts/column/Column';
import {RowRight} from '../../../components/layouts/row/Row';
import {Loader} from '../../../components/loading/Loader';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
import {TableInfoText} from '../../../components/table/TableInfoText';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {getDomainModel, getError} from '../../../state/domain-models/domainModelsSelectors';
import {Organisation} from '../../../state/domain-models/organisation/organisationModels';
import {
  clearOrganisationErrors,
  deleteOrganisation,
  fetchOrganisations,
} from '../../../state/domain-models/organisation/organisationsApiActions';
import {ClearError, ErrorResponse, Fetch, OnClickWithId, uuid} from '../../../types/Types';

const renderParentOrganisation = ({parent}: Organisation) => parent ? parent.name : '';
const renderName = ({name}: Organisation) => name;
const renderSlug = ({slug}: Organisation) => slug;

interface StateToProps {
  organisations: DomainModel<Organisation>;
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
}

interface DispatchToProps {
  deleteOrganisation: OnClickWithId;
  fetchOrganisations: Fetch;
  clearError: ClearError;
}

interface State {
  isDeleteDialogOpen: boolean;
  organisationToDelete?: uuid;
}

type Props = StateToProps & DispatchToProps;

class OrganisationsComponent extends React.Component<Props, State> {

  state: State = {isDeleteDialogOpen: false};

  componentDidMount() {
    this.props.fetchOrganisations();
  }

  componentWillReceiveProps({fetchOrganisations}: Props) {
    fetchOrganisations();
  }

  render() {
    const {
      organisations,
      isFetching,
      error,
      clearError,
    } = this.props;

    const renderActionDropdown = ({id}: Organisation) =>
      <OrganisationActions confirmDelete={this.openDialog} id={id}/>;

    return (
      <Loader isFetching={isFetching} error={error} clearError={clearError}>
        <Column>
          <RowRight>
            <OrganisationBatchActions/>
          </RowRight>
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
          <TableInfoText/>
          <ConfirmDialog
            isOpen={this.state.isDeleteDialogOpen}
            close={this.closeDialog}
            confirm={this.deleteSelectedOrganisation}
          />
        </Column>
      </Loader>
    );
  }

  openDialog = (id: uuid) => this.setState({isDeleteDialogOpen: true, organisationToDelete: id});

  closeDialog = () => this.setState({isDeleteDialogOpen: false});

  deleteSelectedOrganisation = () => this.props.deleteOrganisation(this.state.organisationToDelete!);
}

const mapStateToProps = ({domainModels: {organisations}}: RootState): StateToProps => ({
  organisations: getDomainModel(organisations),
  isFetching: organisations.isFetching,
  error: getError(organisations),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  deleteOrganisation,
  fetchOrganisations,
  clearError: clearOrganisationErrors,
}, dispatch);

export const OrganisationsContainer = connect<StateToProps, DispatchToProps>(
  mapStateToProps,
  mapDispatchToProps,
)(OrganisationsComponent);
