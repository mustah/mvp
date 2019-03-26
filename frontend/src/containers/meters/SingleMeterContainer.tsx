import {IconButton} from 'material-ui';
import Paper from 'material-ui/Paper';
import BackIcon from 'material-ui/svg-icons/navigation/arrow-back';
import * as React from 'react';
import {connect} from 'react-redux';
import {RouteComponentProps} from 'react-router';
import {mainContentPaperStyle, svgIconProps} from '../../app/themes';
import {Maybe} from '../../helpers/Maybe';
import {history} from '../../index';
import {firstUpperTranslated} from '../../services/translationService';
import {uuid} from '../../types/Types';
import {MeterDetailsContainer} from '../dialogs/MeterDetailsContainer';
import {PageLayout} from '../PageLayout';

type Props = RouteComponentProps<{id: uuid, collectionPeriod: string}>;

const SingleMeter = ({match: {params: {id, collectionPeriod}}}: Props) => {
  const useCollectionPeriod: boolean = collectionPeriod !== undefined;
  const close = () => (history.goBack());
  return (
    <PageLayout>
      <Paper style={mainContentPaperStyle}>
        <IconButton onClick={close} title={firstUpperTranslated('back')}>
          <BackIcon {...svgIconProps}/>
        </IconButton>

        <MeterDetailsContainer selectedId={Maybe.just(id)} useCollectionPeriod={useCollectionPeriod}/>
      </Paper>
    </PageLayout>
  );
};

export const SingleMeterContainer = connect<null, null, Props>(null, null)(SingleMeter);
