import * as React from 'react';
import {ButtonLinkRed} from '../../../../components/buttons/ButtonLink';
import {ButtonSave} from '../../../../components/buttons/ButtonSave';
import {ThemeContext} from '../../../../components/hoc/withThemeProvider';
import {Column} from '../../../../components/layouts/column/Column';
import {Row} from '../../../../components/layouts/row/Row';
import {config} from '../../../../config/config';
import {firstUpperTranslated} from '../../../../services/translationService';
import {Organisation} from '../../../../state/domain-models/organisation/organisationModels';
import {
  AssetTyped,
  AssetTypeForOrganisation,
  OrganisationAssetType
} from '../../../../state/domain-models/organisation/organisationsApiActions';
import '../../components/OrganisationForm.scss';
import {ColorPickersContainer} from '../containers/ColorPickersContainer';

export interface AssetFormProps {
  organisation: Organisation;
  resetAsset: (parameters: AssetTypeForOrganisation) => void;
  uploadAsset: (formData: FormData, parameters: AssetTypeForOrganisation) => void;
}

const SingleAssetFrom =
  ({
    cssStyles: {primary},
    organisation: {id: organisationId, slug},
    resetAsset,
    uploadAsset,
    assetType
  }: AssetFormProps & AssetTyped & ThemeContext) => {
    const [selectedFile, selectFile] = React.useState<undefined | string | Blob>(undefined);

    const apiUrl = config().axios.baseURL;
    const cacheBust = Math.random().toString(36).substr(2);
    const assetUrl = `${apiUrl}/organisations/${slug}/assets/${assetType}?${cacheBust}`;

    const onSelectFile = (event) => selectFile(event.target.files[0]);

    const onResetAsset = () => resetAsset({organisationId, assetType});

    const onSubmit = () => {
      if (selectedFile) {
        const data = new FormData();
        data.append('asset', selectedFile as Blob);
        uploadAsset(data, {organisationId, assetType});
      }
    };

    const previewBackgroundStyle: React.CSSProperties =
      assetType === OrganisationAssetType.logotype ? {backgroundColor: primary.bgDark} : {};

    const uploadSubmit = selectedFile && (
      <Row style={{paddingTop: 32}}>
        <form onSubmit={onSubmit}>
          <ButtonSave type="submit"/>
        </form>
      </Row>
    );

    return (
      <Row className="configuration-section">
        <Row className="flex-fill-horizontally">
          <h3 style={{marginBottom: 16}}>{firstUpperTranslated(assetType)}</h3>
        </Row>
        <Row className="flex-fill-horizontally preview-container">
          <img alt="" style={{...previewBackgroundStyle}} src={assetUrl}/>
        </Row>
        <Row className="flex-fill-horizontally" style={{paddingTop: 32}}>
          <input name="asset" onChange={onSelectFile} accept=".gif,.png,.jpg,.jpeg" type="file"/>
          <ButtonLinkRed className="Row-center" onClick={onResetAsset}>
            {firstUpperTranslated('use default')}
          </ButtonLinkRed>
        </Row>
        {uploadSubmit}
      </Row>
    );
  };

export const OrganisationAssetForms = (props: AssetFormProps & ThemeContext) => {
  const assetTypeForms = Object.keys(OrganisationAssetType)
    .map((assetType: OrganisationAssetType) => <SingleAssetFrom {...props} assetType={assetType} key={assetType}/>);

  return (
    <Row className="flex-fill-horizontally configuration-section">
      <Column className="one-third">
        <h2>{firstUpperTranslated('visual identity')}</h2>
        <p>{firstUpperTranslated('valid file formats: png, jpg, jpeg and gif')}</p>
      </Column>
      <Column className="two-thirds">
        <Row className="configuration-section">
          <Row className="flex-fill-horizontally">
            <h3 style={{marginBottom: 16}}>{firstUpperTranslated('change colors')}</h3>
          </Row>
          <ColorPickersContainer organisation={props.organisation}/>
        </Row>

        {assetTypeForms}

      </Column>
    </Row>
  );
};
