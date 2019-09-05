import {DataResult, process, State} from '@progress/kendo-data-query';
import {Grid, GridColumn, GridColumnProps} from '@progress/kendo-react-grid';
import * as React from 'react';
import {makeGridClassName} from '../../../app/themes';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
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

interface Props extends ThemeContext, WithEmptyContentProps {
  columns: GridColumnProps[];
  data: DataResult;
}

const GridContent = withEmptyContent<Props>(({cssStyles, data, columns}: Props) => (
  <Column className="Grouping-grid">
    <Grid
      className={makeGridClassName(cssStyles)}
      scrollable="none"
      data={data}
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
        title={firstUpperTranslated('readout')}
      />
      {columns.map((it, index) => <GridColumn {...it} orderIndex={index} key={it.title}/>)}
    </Grid>
    <TimestampInfoMessage/>
  </Column>
));

export const MeasurementList = ({cssStyles, measurements}: MeasurementListProps & ThemeContext) => {
  const [listItems, columns] = React.useMemo(() => renderColumns(measurements), [measurements]);

  const props: Props = {
    columns,
    cssStyles,
    data: process(listItems, state),
    hasContent: listItems.length < 500,
    noContentText:
      firstUpperTranslated('measurements are not displayed. Use the excel export above for complete list.')
  };

  return <GridContent {...props}/>;
};
