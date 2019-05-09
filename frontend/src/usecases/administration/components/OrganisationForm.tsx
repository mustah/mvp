import {find, values} from 'lodash';
import Paper from 'material-ui/Paper';
import * as React from 'react';
import {RouteComponentProps} from 'react-router';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {paperStyle} from '../../../app/themes';
import {OrganisationAssetForm} from '../../../components/forms/OrganisationAssetForm';
import {OrganisationEditForm} from '../../../components/forms/OrganisationEditForm';
import {AdminPageLayout} from '../../../components/layouts/layout/PageLayout';
import {RowIndented} from '../../../components/layouts/row/Row';
import {RetryLoader} from '../../../components/loading/Loader';
import {MainTitle} from '../../../components/texts/Titles';
import {Maybe} from '../../../helpers/Maybe';
import {translate} from '../../../services/translationService';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {Organisation} from '../../../state/domain-models/organisation/organisationModels';
import {AssetTypeForOrganisation} from '../../../state/domain-models/organisation/organisationsApiActions';
import {UserSelection} from '../../../state/user-selection/userSelectionModels';
import {
  CallbackWithData,
  CallbackWithDataAndUrlParameters,
  ClearError,
  ErrorResponse,
  Fetch,
  uuid
} from '../../../types/Types';
import './OrganisationForm.scss';

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
  uploadAsset: (formData: FormData, parameters: AssetTypeForOrganisation) => void;
  resetAsset: (parameters: AssetTypeForOrganisation) => void;
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
  updateOrganisation,
  uploadAsset,
  resetAsset,
}: Props) => {
  React.useEffect(() => {
    fetchUserSelections();
    fetchOrganisations();
  });

  const userSelections: UserSelection[] = values(selections);
  const organisation: Organisation | undefined = find(organisations, {id: organisationId});

  // TODO what happens if the name/slug of an organisation changes from underneath? :S can we make "edit organisation
  // name" super admin-only? or maybe dev-only?
  const organisationAssetForm = organisation
    ? (
      <OrganisationAssetForm
        id={organisation.id}
        slug={organisation.slug}
        uploadAsset={uploadAsset}
        resetAsset={resetAsset}
      />
    )
    : null;

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
            <RowIndented className="OrganisationForm">
              <OrganisationEditForm
                addOrganisation={addOrganisation}
                addSubOrganisation={addSubOrganisation}
                organisations={organisations}
                organisation={organisation}
                updateOrganisation={updateOrganisation}
                selections={userSelections}
              />
              {organisationAssetForm}
            </RowIndented>
          </RetryLoader>
        </RetryLoader>
      </Paper>
    </AdminPageLayout>
  );
};
