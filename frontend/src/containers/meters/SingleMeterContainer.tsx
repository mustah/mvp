import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {RouteComponentProps} from 'react-router';
import {mainContentPaperStyle} from '../../app/themes';
import {Maybe} from '../../helpers/Maybe';
import {uuid} from '../../types/Types';
import {MeterDetailsContainer} from '../dialogs/MeterDetailsContainer';
import {PageLayout} from '../PageLayout';

type Props = RouteComponentProps<{id: uuid, collectionPeriod: string}>;

const SingleMeter = ({match: {params: {id, collectionPeriod}}}: Props) => {
  const useCollectionPeriod: boolean = collectionPeriod !== undefined;
  return (
    <PageLayout>
      <Paper style={mainContentPaperStyle}>
          <MeterDetailsContainer selectedId={Maybe.just(id)} useCollectionPeriod={useCollectionPeriod}/>
      </Paper>
    </PageLayout>
  );
};

export const SingleMeterContainer = connect<null, null, Props>(null, null)(SingleMeter);
