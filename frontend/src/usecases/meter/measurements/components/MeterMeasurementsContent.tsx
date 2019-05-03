import * as React from 'react';
import {Column} from '../../../../components/layouts/column/Column';
import {ToolbarView} from '../../../../state/ui/toolbar/toolbarModels';
import {MeterMeasurementsContainer} from '../containers/MeterMeasurementsContainer';
import {MeterMeasurementsToolbarContainer} from '../containers/MeterMeasurementsToolbarContainer';
import {MeterDetailProps} from '../meterDetailModels';

const isVisible = (show: boolean): string => show ? 'flex' : 'none';

export const MeterMeasurementsContent = ({view, meter, useCollectionPeriod}: MeterDetailProps) =>
  (
    <Column>
      <MeterMeasurementsToolbarContainer meter={meter} useCollectionPeriod={useCollectionPeriod}/>
      <Column style={{display: isVisible(view === ToolbarView.table)}}>
        <MeterMeasurementsContainer meter={meter} useCollectionPeriod={useCollectionPeriod}/>
      </Column>
    </Column>
  );
