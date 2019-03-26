import {IconButton} from 'material-ui';
import Paper from 'material-ui/Paper';
import BackIcon from 'material-ui/svg-icons/navigation/arrow-back';
import * as React from 'react';
import {RouteComponentProps} from 'react-router';
import {history} from '../../..';
import {iconStyle, mainContentPaperStyle, svgIconProps} from '../../../app/themes';
import {PageLayout} from '../../../components/layouts/layout/PageLayout';
import {RowSpaceBetween} from '../../../components/layouts/row/Row';
import {MeterDetailsContainer} from '../../../containers/dialogs/MeterDetailsContainer';
import {Maybe} from '../../../helpers/Maybe';
import {firstUpperTranslated} from '../../../services/translationService';
import {uuid} from '../../../types/Types';

type Props = RouteComponentProps<{id: uuid, collectionPeriod: string}>;

const backButtonStyle: React.CSSProperties = {
  ...iconStyle,
  marginBottom: 14,
};

export const MeterDetailsPage = ({match: {params: {id, collectionPeriod}}}: Props) =>
  (
    <PageLayout>
      <RowSpaceBetween>
        <IconButton onClick={history.goBack} title={firstUpperTranslated('back')} style={backButtonStyle}>
          <BackIcon {...svgIconProps}/>
        </IconButton>
      </RowSpaceBetween>

      <Paper style={mainContentPaperStyle}>
        <MeterDetailsContainer selectedId={Maybe.just(id)} useCollectionPeriod={collectionPeriod !== undefined}/>
      </Paper>
    </PageLayout>
  );
