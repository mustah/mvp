import {Grid, GridColumn, GridNoRecords} from '@progress/kendo-react-grid';
import ActionDelete from 'material-ui/svg-icons/action/delete';
import * as React from 'react';
import {iconSizeMedium, makeGridClassName} from '../../../../../app/themes';
import {ThemeContext, withCssStyles} from '../../../../../components/hoc/withThemeProvider';
import {RowRight} from '../../../../../components/layouts/row/Row';
import {firstUpperTranslated, translate} from '../../../../../services/translationService';
import {toIdNamed} from '../../../../../types/Types';

interface Props extends ThemeContext {
  deviceEuis: string[];
}

export const BatchReferenceDeviceGrid = withCssStyles(({cssStyles, deviceEuis}: Props) => {

  const renderDeleteIcon = ({dataItem: {id}}) => (
    <td>
      <RowRight>
        <ActionDelete className="clickable" style={iconSizeMedium} onClick={ev => console.log('...remove', id)}/>
      </RowRight>
    </td>
  );

  return (
    <Grid
      className={makeGridClassName(cssStyles)}
      style={{borderTopWidth: 1}}
      data={deviceEuis.map(toIdNamed)}
      scrollable="none"
    >
      <GridNoRecords>{firstUpperTranslated('no devices')}</GridNoRecords>
      <GridColumn
        sortable={false}
        field="id"
        title={translate('device eui')}
        width={256}
        headerClassName="left-most"
        className="left-most"
      />
      <GridColumn cell={renderDeleteIcon} width={54}/>
    </Grid>
  );
});
