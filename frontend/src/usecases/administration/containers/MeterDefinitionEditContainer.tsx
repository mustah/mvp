import {values} from 'lodash';
import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {RouteComponentProps} from 'react-router';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {paperStyle} from '../../../app/themes';
import {MeterDefinitionEditForm} from '../../../components/forms/MeterDefinitionEditForm';
import {RowIndented} from '../../../components/layouts/row/Row';
import {Loader} from '../../../components/loading/Loader';
import {MainTitle} from '../../../components/texts/Titles';
import {AdminPageLayout} from '../../../containers/PageLayout';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {getEntitiesDomainModels, getError} from '../../../state/domain-models/domainModelsSelectors';
import {clearMediumsErrors, fetchMediums} from '../../../state/domain-models/medium/mediumModels';
import {Medium, MeterDefinition, Quantity} from '../../../state/domain-models/meter-definitions/meterDefinitionModels';
import {
  addMeterDefinition,
  fetchMeterDefinitions,
  updateMeterDefinition
} from '../../../state/domain-models/meter-definitions/meterDefinitionsApiActions';
import {Organisation} from '../../../state/domain-models/organisation/organisationModels';
import {
  clearOrganisationErrors,
  fetchOrganisations,
} from '../../../state/domain-models/organisation/organisationsApiActions';
import {getOrganisations} from '../../../state/domain-models/organisation/organisationSelectors';
import {clearQuantityErrors, fetchQuantities} from '../../../state/domain-models/quantities/quantitesApiActions';
import {CallbackWithData, ClearError, ErrorResponse, Fetch} from '../../../types/Types';

interface StateToProps {
  meterDefinitions: ObjectsById<MeterDefinition>;
  organisations: Organisation[];
  mediums: ObjectsById<Medium>;
  isFetchingOrganisations: boolean;
  isFetchingMeterDefinitions: boolean;
  isFetchingMediums: boolean;
  organisationsError: Maybe<ErrorResponse>;
  mediumsError: Maybe<ErrorResponse>;
  quantities: ObjectsById<Quantity>;
  isFetchingQuantities: boolean;
  quantitiesErrors: Maybe<ErrorResponse>;
}

interface DispatchToProps {
  addMeterDefinition: CallbackWithData;
  updateMeterDefinition: CallbackWithData;
  fetchMeterDefinitions: Fetch;
  fetchOrganisations: Fetch;
  fetchMediums: Fetch;
  clearOrganisationErrors: ClearError;
  clearMediumsErrors: ClearError;
  fetchQuantities: Fetch;
  clearQuantityErrors: ClearError;
}

type OwnProps = InjectedAuthRouterProps & RouteComponentProps<{meterDefinitionId: number}>;
type Props = OwnProps & StateToProps & DispatchToProps;

class MeterDefinitionEdit extends React.Component<Props, {}> {

  componentDidMount() {
    this.props.fetchOrganisations();
    this.props.fetchMediums();
  }

  componentWillReceiveProps({fetchOrganisations, fetchMediums, fetchQuantities}: Props) {
    fetchOrganisations();
    fetchMediums();
    fetchQuantities();
  }

  render() {
    const {
      meterDefinitions,
      addMeterDefinition,
      organisations,
      isFetchingOrganisations,
      isFetchingMediums,
      organisationsError,
      mediumsError,
      clearOrganisationErrors,
      clearMediumsErrors,
      match: {params: {meterDefinitionId}},
      updateMeterDefinition,
      mediums,
      isFetchingQuantities,
      quantitiesErrors,
      clearQuantityErrors,
      quantities
    } = this.props;

    const title: string =
      meterDefinitionId
        ? translate('edit meter definition')
        : translate('add meter definition');

    return (
      <AdminPageLayout>
        <MainTitle>{title}</MainTitle>

        <Paper style={paperStyle}>
          <RowIndented>
            <Loader
              isFetching={isFetchingOrganisations}
              error={organisationsError}
              clearError={clearOrganisationErrors}
            >
              <Loader
                isFetching={isFetchingMediums}
                error={mediumsError}
                clearError={clearMediumsErrors}
              >
                <Loader
                  isFetching={isFetchingQuantities}
                  error={quantitiesErrors}
                  clearError={clearQuantityErrors}
                >
                <MeterDefinitionEditForm
                  addMeterDefinition={addMeterDefinition}
                  organisations={organisations}
                  meterDef={meterDefinitions[meterDefinitionId]}
                  updateMeterDefinition={updateMeterDefinition}
                  mediums={values(mediums)}
                  allQuantities={values(quantities)}
                />
                </Loader>
              </Loader>
            </Loader>
          </RowIndented>
        </Paper>
      </AdminPageLayout>
    );
  }
}

const mapStateToProps = (
  {auth, domainModels: {organisations, mediums, meterDefinitions, quantities}}: RootState
): StateToProps => ({
  meterDefinitions: getEntitiesDomainModels(meterDefinitions), // TODO why do this differ from organisation?
  organisations: getOrganisations(organisations),
  mediums: getEntitiesDomainModels(mediums),
  isFetchingOrganisations: organisations.isFetching,
  isFetchingMeterDefinitions: meterDefinitions.isFetching,
  isFetchingMediums: mediums.isFetching,
  organisationsError: getError(organisations),
  mediumsError: getError(mediums),
  quantities: getEntitiesDomainModels(quantities),
  isFetchingQuantities: quantities.isFetching,
  quantitiesErrors: getError(quantities),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addMeterDefinition,
  updateMeterDefinition,
  fetchMeterDefinitions,
  fetchOrganisations,
  fetchMediums,
  clearOrganisationErrors,
  clearMediumsErrors,
  fetchQuantities,
  clearQuantityErrors,
}, dispatch);

export const MeterDefinitionEditContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(MeterDefinitionEdit);
