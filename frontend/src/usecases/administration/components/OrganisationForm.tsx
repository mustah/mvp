import {find, values} from 'lodash';
import Paper from 'material-ui/Paper';
import * as React from 'react';
import {RouteComponentProps} from 'react-router';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {paperStyle} from '../../../app/themes';
import {OrganisationEditForm} from '../../../components/forms/OrganisationEditForm';
import {AdminPageLayout} from '../../../components/layouts/layout/PageLayout';
import {RetryLoader} from '../../../components/loading/Loader';
import {MainTitle} from '../../../components/texts/Titles';
import {Maybe} from '../../../helpers/Maybe';
import {translate} from '../../../services/translationService';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {Organisation} from '../../../state/domain-models/organisation/organisationModels';
import {UserSelection} from '../../../state/user-selection/userSelectionModels';
import {
  CallbackWithData,
  CallbackWithDataAndUrlParameters,
  ClearError,
  ErrorResponse,
  Fetch,
  uuid
} from '../../../types/Types';

export interface StateToProps {
  isFetchingOrganisations: boolean;
  isFetchingUserSelections: boolean;
  organisations: Organisation[];
  organisationsError: Maybe<ErrorResponse>;
  userSelectionsError: Maybe<ErrorResponse>;
  selections: ObjectsById<UserSelection>;
}

export interface DispatchToProps {
  addOrganisation: CallbackWithData;
  addSubOrganisation: CallbackWithDataAndUrlParameters;
  fetchOrganisations: Fetch;
  fetchUserSelections: Fetch;
  clearOrganisationErrors: ClearError;
  clearUserSelectionErrors: ClearError;
  updateOrganisation: CallbackWithData;
}

type OwnProps = InjectedAuthRouterProps & RouteComponentProps<{organisationId: uuid}>;
type Props = OwnProps & StateToProps & DispatchToProps;

export const OrganisationForm = ({
  addOrganisation,
  addSubOrganisation,
  clearOrganisationErrors,
  clearUserSelectionErrors,
  fetchOrganisations,
  fetchUserSelections,
  isFetchingOrganisations,
  isFetchingUserSelections,
  organisationsError,
  match: {params: {organisationId}},
  organisations,
  selections,
  userSelectionsError,
  updateOrganisation
}: Props) => {
  React.useEffect(() => {
    fetchUserSelections();
    fetchOrganisations();
  });

  const userSelections: UserSelection[] = values(selections);
  const organisation: Organisation | undefined = find(organisations, {id: organisationId});

  return (
    <AdminPageLayout>
      <MainTitle>{organisationId ? translate('edit organisation') : translate('add organisation')}</MainTitle>

      <Paper style={paperStyle}>
        <RetryLoader
          isFetching={isFetchingOrganisations}
          error={organisationsError}
          clearError={clearOrganisationErrors}
        >
          <RetryLoader
            isFetching={isFetchingUserSelections}
            error={userSelectionsError}
            clearError={clearUserSelectionErrors}
          >
            <OrganisationEditForm
              addOrganisation={addOrganisation}
              addSubOrganisation={addSubOrganisation}
              organisations={organisations}
              organisation={organisation}
              updateOrganisation={updateOrganisation}
              selections={userSelections}
            />
          </RetryLoader>
        </RetryLoader>
      </Paper>
    </AdminPageLayout>
  );
};
