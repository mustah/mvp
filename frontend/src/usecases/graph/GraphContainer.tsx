import 'GraphContainer.scss';
import RaisedButton from 'material-ui/RaisedButton';
import * as React from 'react';
import {CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis} from 'recharts';
import {RowCenter} from '../common/components/layouts/row/Row';

export const GraphContainer = (props: any) => {
  /**
   * protip for generating these numbers:
   * $ python
   * >>> import random as r
   * >>> for _ in range(7): str(r.randrange(63, 82)) + "." + str(r.randrange(0, 999))
   * ...
   * '78.583'
   * '77.715'
   * '73.950'
   * '77.798'
   * '66.593'
   * '81.817'
   * '69.131'
   */

  const data = [
    {name: '15 nov 09:00', asdf1: 68.423, asdf2: 71.505, asdf0: 81.817},
    {name: '15 nov 10:00', asdf1: 65.590, asdf2: 71.318, asdf0: 69.131},
    {name: '15 nov 11:00', asdf1: 78.583, asdf2: 73.830, asdf0: 69.586},
    {name: '15 nov 12:00', asdf1: 77.715, asdf2: 77.821, asdf0: 72.874},
    {name: '15 nov 13:00', asdf1: 73.950, asdf2: 73.661, asdf0: 66.502},
    {name: '15 nov 14:00', asdf1: 77.798, asdf2: 75.292, asdf0: 80.894},
    {name: '15 nov 15:00', asdf1: 66.593, asdf2: 81.150, asdf0: 74.864},
  ];

  const labels = {
    asdf0: 'Medel i Perstorp',
    asdf1: 'Mätare 67606252',
    asdf2: 'Mätare 67190406',
  };

  // ResponsiveContainer is a bit weird, if we leave out the dimensions of the containing <div>, it breaks
  // Setting width of ResponsiveContainer to 100% will case the menu to overlap when toggled
  return (
    <div style={{width: '100%', height: '100%'}}>
      <RowCenter>
        <RaisedButton primary={true} label="Energi"/>
        <RaisedButton label="Volym"/>
        <RaisedButton label="Effekt"/>
        <RaisedButton label="Flöde"/>
        <RaisedButton label="Flödestemp."/>
        <RaisedButton label="Returtemp."/>
        <RaisedButton label="Temp. skillnad"/>
      </RowCenter>

      <ResponsiveContainer width="80%" aspect={4.0 / 1.0}>
        <LineChart
          width={10}
          height={30}
          data={data}
          margin={{top: 5, right: 30, left: 20, bottom: 5}}
        >
          <XAxis dataKey="name"/>
          <YAxis label="MWh" yAxisId="left"/>
          <YAxis yAxisId="right" orientation="right"/>
          <CartesianGrid strokeDasharray="3 3"/>
          <Tooltip/>
          <Legend/>
          <Line yAxisId="left" name={labels.asdf0} type="monotone" dataKey="asdf0" stroke="#8884d8" activeDot={{r: 8}}/>
          <Line yAxisId="right" name={labels.asdf1} type="monotone" dataKey="asdf1" stroke="#82ca9d"/>
          <Line yAxisId="right" name={labels.asdf2} type="monotone" dataKey="asdf2" stroke="#ffb4a4"/>
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};
