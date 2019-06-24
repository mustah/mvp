import {values} from 'lodash';
import Paper from 'material-ui/Paper';
import * as React from 'react';
import {RouteComponentProps} from 'react-router';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {paperStyle} from '../../../../app/themes';
import {MeterDefinitionEditForm} from '../../../../components/forms/MeterDefinitionEditForm';
import {AdminPageLayout} from '../../../../components/layouts/layout/PageLayout';
import {Loader} from '../../../../components/loading/Loader';
import {MainTitle} from '../../../../components/texts/Titles';
import {Maybe} from '../../../../helpers/Maybe';
import {translate} from '../../../../services/translationService';
import {ObjectsById} from '../../../../state/domain-models/domainModels';
import {
  Medium,
  MeterDefinition,
  Quantity
} from '../../../../state/domain-models/meter-definitions/meterDefinitionModels';
import {Organisation} from '../../../../state/domain-models/organisation/organisationModels';
import {CallbackWith, ErrorResponse, Fetch} from '../../../../types/Types';

export interface StateToProps {
  meterDefinitions: ObjectsById<MeterDefinition>;
  organisations: Organisation[];
  mediums: ObjectsById<Medium>;
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
  quantities: ObjectsById<Quantity>;
}

export interface DispatchToProps {
  addMeterDefinition: CallbackWith<MeterDefinition>;
  updateMeterDefinition: CallbackWith<MeterDefinition>;
  fetchMeterDefinitions: Fetch;
  fetchOrganisations: Fetch;
  fetchMediums: Fetch;
  fetchQuantities: Fetch;
}

export type OwnProps = InjectedAuthRouterProps & RouteComponentProps<{meterDefinitionId: string}>;
type Props = OwnProps & StateToProps & DispatchToProps;

export const MeterDefinitionEdit = (props: Props) => {
  const {
    meterDefinitions,
    addMeterDefinition,
    organisations,
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

  return (
    <AdminPageLayout>
      <MainTitle>
        {meterDefinitionId ? translate('edit meter definition') : translate('add meter definition')}
      </MainTitle>

      <Paper style={{...paperStyle, padding: 24}}>
        <Loader isFetching={isFetching}>
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
      </Paper>
    </AdminPageLayout>
  );
};
