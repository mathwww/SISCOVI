package DAO;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import com.sun.scenario.effect.impl.prism.ps.PPSBlend_REDPeer;
import sun.dc.pr.PRError;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

public class AuxiliateCalcDAO {
    private Connection connection;
    public AuxiliateCalcDAO(Connection connection){
        this.connection = connection;
    }


}
