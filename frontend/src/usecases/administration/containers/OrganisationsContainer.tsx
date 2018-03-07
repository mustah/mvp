import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {OrganisationActionsDropdown} from '../../../components/actions-dropdown/OrganisationActionsDropdown';
import {OrganisationsActionsDropdown} from '../../../components/actions-dropdown/OrganisationsActionsDropdown';
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
import {getDomainModel, getError} from '../../../state/domain-models/domainModelsSelectors';
import {
  clearOrganisationErrors,
  deleteOrganisation,
  fetchOrganisations,
} from '../../../state/domain-models/organisation/organisationsApiActions';
import {Organisation} from '../../../state/domain-models/organisation/organisationModels';
import {ClearError, ErrorResponse, OnClickWithId, RestGet, uuid} from '../../../types/Types';

interface StateToProps {
  organisations: DomainModel<Organisation>;
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
}

interface DispatchToProps {
  deleteOrganisation: OnClickWithId;
  fetchOrganisations: RestGet;
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

    const renderName = ({name}: Organisation) => name;
    const renderCode = ({code}: Organisation) => code;
    const renderActionDropdown = ({id}: Organisation) =>
      <OrganisationActionsDropdown confirmDelete={this.openDialog} id={id}/>;

    return (
      <Loader isFetching={isFetching} error={error} clearError={clearError}>
        <Column>
          <RowRight>
            <OrganisationsActionsDropdown/>
          </RowRight>
          <Table result={organisations.result} entities={organisations.entities}>
            <TableColumn
              header={<TableHead className="first">{translate('name')}</TableHead>}
              renderCell={renderName}
            />
            <TableColumn
              header={<TableHead>{translate('code')}</TableHead>}
              renderCell={renderCode}
            />
            <TableColumn
              header={<TableHead className="actionDropdown">{' '}</TableHead>}
              renderCell={renderActionDropdown}
            />
          </Table>
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
