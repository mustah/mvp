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
import {isDefined} from '../../../helpers/commonHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {getEntitiesDomainModels} from '../../../state/domain-models/domainModelsSelectors';
import {clearMediumsErrors, fetchMediums} from '../../../state/domain-models/medium/mediumModels';
import {Medium, MeterDefinition, Quantity} from '../../../state/domain-models/meter-definitions/meterDefinitionModels';
import {
  addMeterDefinition, clearMeterDefinitionErrors,
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
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
  quantities: ObjectsById<Quantity>;
}

interface DispatchToProps {
  addMeterDefinition: CallbackWithData;
  updateMeterDefinition: CallbackWithData;
  fetchMeterDefinitions: Fetch;
  fetchOrganisations: Fetch;
  fetchMediums: Fetch;
  clearErrors: ClearError;
  fetchQuantities: Fetch;
}

type OwnProps = InjectedAuthRouterProps & RouteComponentProps<{meterDefinitionId: number}>;
type Props = OwnProps & StateToProps & DispatchToProps;

const MeterDefinitionEdit = (props: Props) => {
  const {
    meterDefinitions,
    addMeterDefinition,
    organisations,
    clearErrors,
    error,
    fetchMediums,
    fetchMeterDefinitions,
    fetchOrganisations,
    fetchQuantities,
    isFetching,
    match: {params: {meterDefinitionId}},
    updateMeterDefinition,
    mediums,
    quantities
  } = props;
  React.useEffect(() => {
    fetchMediums();
    fetchOrganisations();
    fetchQuantities();
    fetchMeterDefinitions();
  }, [props]);

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
            isFetching={isFetching}
            error={error}
            clearError={clearErrors}
          >
            <MeterDefinitionEditForm
              key={`meter-definition-${meterDefinitionId}`}
              addMeterDefinition={addMeterDefinition}
              organisations={organisations}
              meterDef={meterDefinitions[meterDefinitionId]}
              updateMeterDefinition={updateMeterDefinition}
              mediums={values(mediums)}
              allQuantities={values(quantities)}
            />
          </Loader>
        </RowIndented>
      </Paper>
    </AdminPageLayout>
  );
};

const mapStateToProps = (
  {auth, domainModels: {organisations, mediums, meterDefinitions, quantities}}: RootState
): StateToProps => {
  const errors = [organisations.error, mediums.error, quantities.error, meterDefinitions.error];
  return ({
    meterDefinitions: getEntitiesDomainModels(meterDefinitions), // TODO why do this differ from organisation?
    organisations: getOrganisations(organisations),
    mediums: getEntitiesDomainModels(mediums),
    isFetching: organisations.isFetching || meterDefinitions.isFetching || mediums.isFetching || quantities.isFetching,
    error: Maybe.maybe(errors.find(isDefined)),
    quantities: getEntitiesDomainModels(quantities),
  });
};

const clearErrors = () => {
  clearQuantityErrors();
  clearMediumsErrors();
  clearMeterDefinitionErrors();
  clearOrganisationErrors();
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addMeterDefinition,
  clearErrors,
  updateMeterDefinition,
  fetchMeterDefinitions,
  fetchOrganisations,
  fetchMediums,
  fetchQuantities
}, dispatch);

export const MeterDefinitionEditContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(MeterDefinitionEdit);
