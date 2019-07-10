import {DataResult, process, State} from '@progress/kendo-data-query';
import {Grid, GridColumn} from '@progress/kendo-react-grid';
import * as React from 'react';
import {makeGridClassName} from '../../../app/themes';
import {ThemeContext} from '../../../components/hoc/withThemeProvider';
import {Column} from '../../../components/layouts/column/Column';
import {TimestampInfoMessage} from '../../../components/timestamp-info-message/TimestampInfoMessage';
import {firstUpperTranslated} from '../../../services/translationService';
import {MeasurementsApiResponse} from '../../../state/ui/graph/measurement/measurementModels';
import {cellRender, headerCellRender, renderColumns, rowRender} from '../helpers/measurementGridHelper';

export interface MeasurementListProps {
  measurements: MeasurementsApiResponse;
}

const state: State = {group: [{field: 'label', dir: 'desc'}], sort: [{field: 'label', dir: 'asc'}]};

export const MeasurementList = ({cssStyles, measurements}: MeasurementListProps & ThemeContext) => {
  const [listItems, quantityColumns] = React.useMemo(() => renderColumns(measurements), [measurements]);

  const dataResult: DataResult = process(listItems, state);

  return (
    <Column className="Grouping-grid">
      <Grid
        className={makeGridClassName(cssStyles)}
        scrollable="none"
        data={dataResult}
        groupable={true}
        cellRender={cellRender}
        headerCellRender={headerCellRender}
        rowRender={rowRender}
        {...state}
      >
        <GridColumn
          headerClassName="left-most"
          className="left-most"
          field="when"
          key="when"
          title={firstUpperTranslated('readout')}
        />
        {quantityColumns}
      </Grid>
      <TimestampInfoMessage/>
    </Column>
  );
};
